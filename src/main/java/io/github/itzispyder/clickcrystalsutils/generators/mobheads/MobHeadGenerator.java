package io.github.itzispyder.clickcrystalsutils.generators.mobheads;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.itzispyder.clickcrystalsutils.Generator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MobHeadGenerator implements Generator {

    public static void main(String[] args) {
        MobHeadGenerator gen = new MobHeadGenerator("1.21.5", false);
        System.out.println(gen.generateAndCopy());
    }

    private final String ALL_MOB_HEADS_REPOSITORY, ENTITY_TYPES;
    private final Map<String, String> rawTextureMap, texturePathMap;
    private final List<String> entityTypes;
    private boolean raw;

    public MobHeadGenerator(String minecraftVersion, boolean raw) {
        ALL_MOB_HEADS_REPOSITORY = "https://github.com/MLDEG/AllMobHeads/archive/refs/heads/main.zip";
        ENTITY_TYPES = "https://maven.fabricmc.net/docs/yarn-%s+build.1/net/minecraft/entity/EntityType.html".formatted(minecraftVersion);
        this.raw = raw;
        this.rawTextureMap = new HashMap<>();
        this.texturePathMap = new HashMap<>();
        this.entityTypes = fetchEntityTypes();
    }

    public List<String> fetchEntityTypes() {
        System.out.println("fetching entity types...");

        try {
            URL url = URI.create(ENTITY_TYPES).toURL();
            Document doc = Jsoup.parse(url, 0);
            Elements elements = doc.select("code > a.member-name-link");

            List<String> list = new ArrayList<>();
            for (Element element : elements) {
                String text = element.text();
                if (text.matches("[A-Z_]+"))
                    list.add(text.toLowerCase());
            }
            return list;
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public void fetchRepository() throws Exception {
        System.out.println("downloading remote repository...");

        File tempRootFile;
        URL url = URI.create(ALL_MOB_HEADS_REPOSITORY).toURL();
        try (ZipInputStream zip = new ZipInputStream(url.openStream())) {
            ZipEntry entry;
            tempRootFile = new File("temp-all-mob-heads-repository.download");
            tempRootFile.mkdir();

            while ((entry = zip.getNextEntry()) != null) {
                System.out.println("entry -> " + entry.getName());
                File file = new File(tempRootFile, entry.getName());
                if (entry.isDirectory()) {
                    file.mkdirs();
                }
                else try (FileOutputStream fos = new FileOutputStream(file)) {
                    fos.write(zip.readAllBytes());
                }
            }
        }

        System.out.println("file downloaded!");

        scanRepository(tempRootFile);
        deepDelete(tempRootFile);
    }

    private void deepDelete(File file) {
        if (!file.isDirectory()) {
            file.delete();
            return;
        }

        File[] children = file.listFiles();
        if (children == null)
            return;

        for (File child : children)
            deepDelete(child);
        file.delete();
    }

    private void scanRepository(File file) {
        file = file.listFiles()[0]; // first zip dir

        File[] versions = file.listFiles((subFile) ->  {
            return subFile.isDirectory() && subFile.getName().matches("^versions-\\d+-$");
        });

        System.out.printf("scanned %s files in repository".formatted(versions.length));

        for (File version: versions) {
            File collection = new File(version, "data/all_mob_heads/advancement/collection");
            scanCollection(collection);
        }

        System.out.println("SCAN COMPLETE!");
        System.out.println("Creating textures...");

        File destination = new File("src/main/resources/assets/clickcrystals/textures/display/icons/entities/");
        destination.mkdirs();

        int total = entityTypes.size();
        int order = 1;

        System.out.println("matching entity types...");
        for (String entityType : entityTypes)
            System.out.println("entity -> " + entityType);

        for (String entityType: entityTypes) {
            for (Map.Entry<String, String> entry : rawTextureMap.entrySet()) {
                if (entry.getKey().contains(entityType)) {
                    createTexture(destination, entityType, entry.getValue(), order++, total);
                    break;
                }
            }
        }
    }

    private void scanCollection(File file) {
        if (!file.isDirectory()) {
            scanJSON(file);
            return;
        }

        System.out.println("scanning collection -> " + file.getPath());

        File[] children = file.listFiles();
        if (children == null)
            return;

        for (File child: children)
            scanCollection(child);
    }

    private void scanJSON(File file) {
        System.out.println("scanning JSON -> " + file.getPath());

        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file))) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            JsonObject items = json.get("criteria").getAsJsonObject();

            for (Map.Entry<String, JsonElement> entry : items.entrySet()) {
                String texture = entry.getValue().getAsJsonObject()
                        .getAsJsonObject("conditions")
                        .getAsJsonArray("items").get(0).getAsJsonObject()
                        .getAsJsonObject("components")
                        .getAsJsonObject("minecraft:profile")
                        .getAsJsonArray("properties").get(0).getAsJsonObject()
                        .getAsJsonPrimitive("value").getAsString();

                rawTextureMap.put(entry.getKey(), texture);
            }
        }
        catch (Exception ex) {
            System.out.println("JSON SCAN FAILED -> " + file.getPath());
        }
    }

    private void createTexture(File dest, String name, String base64Texture, int order, int total) {
        File file = new File(dest, name + ".png");

        System.out.printf("creating texture (%s / %s) -> %s%n", order, total, file.getPath());

        String decoded = new String(Base64.getDecoder().decode(base64Texture));
        JsonObject obj = JsonParser.parseString(decoded).getAsJsonObject();
        String textureUrl = obj.getAsJsonObject("textures")
                .getAsJsonObject("SKIN")
                .getAsJsonPrimitive("url").getAsString();

        try {
            URL url = URI.create(textureUrl).toURL();
            InputStream is = url.openStream();

            BufferedImage img = ImageIO.read(is);
            is.close();

            BufferedImage save = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
            for (int x = 0; x < 8; x++)
                for (int y = 0; y < 8; y++)
                    save.setRGB(x, y, img.getRGB(x + 8, y + 8));
            ImageIO.write(save, "png", file);

            texturePathMap.put(name, file.getPath());
        }
        catch (Exception ex) {
            System.out.println("TEXTURE CREATION FAILED -> " + file.getPath());
        }
    }

    @Override
    public String generate() {
        if (raw)
            return generateRaw();
        return generateGeneric();
    }

    public String generateGeneric() {
        try {
            fetchRepository();

            StringBuilder builder = new StringBuilder();
            builder.append("""
                    | *Order* | **Entity** | **Texture** |
                    |:--------|:-----------|:------------|
                    """.trim());
            builder.append('\n');

            int order = 1;
            for (String entityType : entityTypes) {
                String path = texturePathMap.get(entityType);
                if (path == null)
                    builder.append("| %s | %s | not a living entity |\n".formatted(order++, entityType, entityType));
                else
                    builder.append("| %s | %s | ![icon.%s](%s) |\n".formatted(order++, entityType, entityType, path));
            }

            return builder.toString();
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public String generateRaw() {
        try {
            fetchRepository();

            StringBuilder builder = new StringBuilder();
            builder.append("""
                    | **Entity** | **Texture** |
                    |:-----------|:------------|
                    """.trim());
            builder.append('\n');

            for (Map.Entry<String, String> entry : rawTextureMap.entrySet())
                builder.append("| %s | %s |\n".formatted(entry.getKey(), entry.getValue()));

            return builder.toString();
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
