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
        MobHeadGenerator gen = new MobHeadGenerator("1.21.5", GenType.CODE);
        System.out.println(gen.generateAndCopy());
    }

    private final String BOSS_PACKAGE, MOBS_PACKAGE, PASSIVE_PACKAGE;
    private final String ALL_MOB_HEADS_REPOSITORY, ENTITY_TYPES;
    private final Map<String, String> rawTextureMap, texturePathMap;
    private final List<String> entityTypes, entityClasses;
    private final List<String> ENTITY_CLASS_BLACKLIST = List.of("LivingEntity", "PatrolEntity");
    private final GenType genType;

    public MobHeadGenerator(String minecraftVersion, GenType genType) {
        ALL_MOB_HEADS_REPOSITORY = "https://github.com/MLDEG/AllMobHeads/archive/refs/heads/main.zip";
        ENTITY_TYPES = "https://maven.fabricmc.net/docs/yarn-%s+build.1/net/minecraft/entity/EntityType.html".formatted(minecraftVersion);
        BOSS_PACKAGE = "https://maven.fabricmc.net/docs/yarn-%s+build.1/net/minecraft/entity/boss/package-summary.html".formatted(minecraftVersion);
        MOBS_PACKAGE = "https://maven.fabricmc.net/docs/yarn-%s+build.1/net/minecraft/entity/mob/package-summary.html".formatted(minecraftVersion);
        PASSIVE_PACKAGE = "https://maven.fabricmc.net/docs/yarn-%s+build.1/net/minecraft/entity/passive/package-summary.html".formatted(minecraftVersion);
        this.entityClasses = fetchEntityClasses();
        this.genType = genType;
        this.rawTextureMap = new HashMap<>();
        this.texturePathMap = new HashMap<>();
        this.entityTypes = fetchEntityTypes();
    }

    private List<String> fetchEntityClasses() {
        System.out.println("fetching entity classes...");

        try {
            List<String> entityTypes = new ArrayList<>();
            entityTypes.addAll(fetchPackage(BOSS_PACKAGE));
            entityTypes.addAll(fetchPackage(MOBS_PACKAGE));
            entityTypes.addAll(fetchPackage(PASSIVE_PACKAGE));
            return entityTypes;
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private List<String> fetchPackage(String packageUrl) throws Exception {
        URL url = URI.create(packageUrl).toURL();
        Document doc = Jsoup.parse(url, 0);
        Elements elements = doc.select("div#class-summary div.summary-table div a");

        List<String> list = new ArrayList<>();
        for (Element element : elements) {
            String text = element.text();
            if (isEntityClassName(text))
                list.add(text);
        }
        return list;
    }

    private boolean isEntityClassName(String name) {
        return name.contains("Entity") && !name.contains(".") && !name.contains("Abstract") && !ENTITY_CLASS_BLACKLIST.contains(name);
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
        try {
            switch (genType) {
                case CODE -> {
                    return generateCode();
                }
                case RAW -> {
                    return generateRaw();
                }
                default -> {
                    return generateGeneric();
                }
            }
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public String generateCode() throws Exception {
        fetchRepository();

        StringBuilder builder = new StringBuilder();

        builder.append("""
                package io.github.itzispyder.clickcrystals.gui.misc.brushes;
                                
                import io.github.itzispyder.clickcrystals.Global;
                import io.github.itzispyder.clickcrystals.util.minecraft.render.RenderUtils;
                import net.minecraft.client.gui.DrawContext;
                import net.minecraft.entity.Entity;
                import net.minecraft.entity.boss.WitherEntity;
                import net.minecraft.entity.mob.*;
                import net.minecraft.entity.passive.*;
                import net.minecraft.util.Identifier;
                                
                import java.util.HashMap;
                import java.util.Map;
                                
                public class MobHeadBrush implements Global {
                                
                    public static final Map<Class<? extends Entity>, Identifier> REGISTRY = new HashMap<>() {{
                """.trim());
        builder.append('\n');

        for (String entityClass : entityClasses) {
            String entityType = entityClass.replaceAll("Entity$", "")
                    .replaceAll("([A-Z])", "_$1")
                    .toLowerCase()
                    .substring(1);

            builder.append("        this.put(%s.class, Identifier.of(modId, \"textures/display/icons/entities/%s.png\"));\n"
                    .formatted(entityClass, entityType));
        }

        builder.append("""
                    }};
                                
                    public static Identifier getIdentifier(Class<? extends Entity> entity) {
                        return REGISTRY.get(entity);
                    }
                                
                    public static void drawHead(DrawContext context, Class<? extends Entity> entity, int x, int y, int size) {
                        Identifier tex = getIdentifier(entity);
                        if (tex != null) {
                            RenderUtils.drawTexture(context,tex, x, y, size, size);
                        }
                    }
                                
                    public static void drawHead(DrawContext context, Entity entity, int x, int y, int size) {
                        drawHead(context, entity.getClass(), x, y, size);
                    }
                }
                """);

        return builder.toString();
    }

    public String generateGeneric() throws Exception {
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

    public String generateRaw() throws Exception {
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

    public enum GenType {
        RAW,
        DEFAULT,
        CODE
    }
}
