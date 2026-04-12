package io.github.itzispyder.clickcrystalsutils.generators.mobheads;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.itzispyder.clickcrystalsutils.util.FileValidationUtils;
import io.github.itzispyder.clickcrystalsutils.util.GithubUtils;
import io.github.itzispyder.clickcrystalsutils.util.StringUtils;
import io.github.itzispyder.clickcrystalsutils.util.nonstatic.Version;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.*;

public class EntityTextures {
    
    private static final String REPO_MC_DATA = "https://github.com/PrismarineJS/minecraft-data/archive/refs/heads/master.zip";
    private static final String REPO_TEXTURE = "https://github.com/MLDEG/AllMobHeads/archive/refs/heads/main.zip";

    private final Map<EntityKey, String> textureData;
    private final Map<EntityKey, String> texturePaths;
    private final Version minecraftVersion;
    private final Map<EntityKey, String> textureOverrides = new HashMap<>() {{
        this.put(new EntityKey("zombie", "Zombie"), "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8vcy5uYW1lbWMuY29tL2kvNzg1MjMxYTA4Y2Y0ZDlmMS5wbmcifX19");
        this.put(new EntityKey("skeleton", "Skeleton"), "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8vcy5uYW1lbWMuY29tL2kvNDU1N2I1MDJjMDQxZDcyNC5wbmcifX19");
        this.put(new EntityKey("creeper", "Creeper"), "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8vcy5uYW1lbWMuY29tL2kvYWQ4MWNiMjk1YTM3OTNmMi5wbmcifX19");
        this.put(new EntityKey("piglin", "Piglin"), "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8vcy5uYW1lbWMuY29tL2kvZGQ1NDM1ZjI2N2ZhOTk1ZC5wbmcifX19");
        this.put(new EntityKey("wither_skeleton", "Wither Skeleton"), "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8vcy5uYW1lbWMuY29tL2kvZmQ0ZTc0MmIyNzU1ZDEyYS5wbmcifX19");
        this.put(new EntityKey("ender_dragon", "Ender Dragon"), "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8vcy5uYW1lbWMuY29tL2kvY2U4YzhkYmE5MGZjY2VmMS5wbmcifX19");
        this.put(new EntityKey("player", "Player"), "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8vcy5uYW1lbWMuY29tL2kvOWYyNGFiNjZiZDRmNjRkZS5wbmcifX19");
    }};

    public EntityTextures(Version minecraftVersion) {
        this.minecraftVersion = minecraftVersion;
        this.textureData = new HashMap<>(textureOverrides);
        this.texturePaths = new HashMap<>();
    }

    public void put(EntityKey entity, String texture) {
        if (!textureData.containsKey(entity))
            textureData.put(entity, texture);
    }

    public void reload() {
        this.textureData.clear();
        this.textureData.putAll(this.textureOverrides);

        List<EntityTextures.EntityKey> entities = fetchEntityTypes();
        Set<Map.Entry<String, String>> textures = fetchEntityTextures().entrySet();
        List<PotentialMismatch> potentialMismatches = new ArrayList<>();

        for (EntityTextures.EntityKey key: entities) {
            Map.Entry<String, String> match = textures.stream().max(Comparator.comparing(entry -> {
                return StringUtils.similarity(entry.getKey(), key.id());
            })).orElseThrow();
            String texture = match.getValue();
            boolean firstAppearance = !textureData.containsKey(key);

            put(key, texture);

            String before = match.getKey();
            String after = key.id();
            System.out.printf("-> repo texture %s mapped to entity %s%n", before, after);
            if (!before.equals(after) && firstAppearance)
                if (textureOverrides.keySet().stream().noneMatch(it -> it.id.equals(after)))
                    potentialMismatches.add(new PotentialMismatch(before, after));
        }

        File destination = new File("src/main/resources/assets/clickcrystals/textures/display/icons/entities/");
        int ordinal = 0;
        int total = textureData.size();
        for (Map.Entry<EntityKey, String> entry : textureData.entrySet())
            createTexture(destination, entry.getKey(), entry.getValue(), ++ordinal, total);
        for (PotentialMismatch mismatch : potentialMismatches)
            System.out.printf("WARNING: potential mismatches for entity %s%n", mismatch);
    }

    public Map<EntityKey, String> getTextureData() {
        return textureData;
    }

    public Map<EntityKey, String> getTexturePaths() {
        return texturePaths;
    }

    public Version getMinecraftVersion() {
        return minecraftVersion;
    }

    public record EntityKey(String id, String displayName) {
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof EntityKey(String oId, String oName)))
                return false;
            return this.id.equals(oId) && this.displayName.equals(oName);
        }
    }

    public record PotentialMismatch(String before, String after) {
        @Override
        public String toString() {
            return "[%s -> %s (%s)]".formatted(before, after, StringUtils.similarity(before, after));
        }
    }

    private List<EntityKey> fetchEntityTypes() {
        File repo = GithubUtils.downloadRepo(REPO_MC_DATA);
        File parent = new File(repo, "data/pc");
        File version = new File(parent, minecraftVersion.toString());

        if (!version.exists()) {
            System.out.printf("WARNING: Could not find entity data for version %s, falling back to latest supported version%n", minecraftVersion);
            File[] files = parent.listFiles(it -> it.getName().matches(Version.PATTERN.pattern()));
            if (files == null || files.length == 0)
                throw new IllegalStateException("Could not find entity data for version " + minecraftVersion);

            Arrays.sort(files, Comparator.comparing(it -> Version.ofString(it.getName())));
            version = files[files.length - 1];
        }

        File entitiesJson = new File(version, "entities.json");
        List<EntityKey> entities = readEntityJson(entitiesJson);
        GithubUtils.deleteRepo(REPO_MC_DATA);

        return entities.stream()
                .sorted(Comparator.comparing(EntityKey::id))
                .toList();
    }

    private List<EntityKey> readEntityJson(File jsonFile) {
        try (FileReader stream = new FileReader(jsonFile)) {
            JsonArray json = JsonParser.parseReader(stream).getAsJsonArray();
            List<EntityKey> entities = new ArrayList<>();

            for (JsonElement entry: json) {
                JsonObject ent = entry.getAsJsonObject();
                String id = ent.get("name").getAsString();
                String displayName = ent.get("displayName").getAsString();
                String type = ent.get("type").getAsString();

                if (!type.matches("player|projectile|other"))
                    entities.add(new EntityKey(id, displayName));
            }
            return entities;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, String> fetchEntityTextures() {
        File repo = GithubUtils.downloadRepo(REPO_TEXTURE);
        Map<String, String> textures = new HashMap<>();
        File[] versions = repo.listFiles(it -> it.getName().matches("overlay-\\d+-(\\d+)?"));

        if (versions == null || versions.length == 0)
            return textures;

        for (File ver: versions) {
            File collections = new File(ver, "data/all_mob_heads/advancement/collection");
            readCollections(collections, textures);
        }

        GithubUtils.deleteRepo(REPO_MC_DATA);
        return textures;
    }

    private void readCollections(File file, Map<String, String> textures) {
        if (!file.isDirectory()) {
            readTextureJson(file, textures);
            return;
        }

        System.out.println("scanning collection -> " + file.getPath());

        File[] children = file.listFiles();
        if (children == null)
            return;

        for (File child: children)
            readCollections(child, textures);
    }

    private void readTextureJson(File file, Map<String, String> textures) {
        System.out.println("scanning JSON -> " + file.getPath());

        try (FileReader reader = new FileReader(file)) {
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
                textures.put(entry.getKey(), texture);
            }
        }
        catch (Exception ex) {
            System.out.println("JSON SCAN FAILED -> " + file.getPath());
        }
    }

    private void createTexture(File dest, EntityKey key, String base64Texture, int order, int total) {
        File file = new File(dest, key.id + ".png");
        FileValidationUtils.validate(file);

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

            texturePaths.put(key, file.getPath().replace(File.separatorChar, '/'));
        }
        catch (Exception ex) {
            System.out.println("TEXTURE CREATION FAILED -> " + file.getPath());
        }
    }
}
