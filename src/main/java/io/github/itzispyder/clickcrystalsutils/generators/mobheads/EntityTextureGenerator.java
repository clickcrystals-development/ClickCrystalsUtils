package io.github.itzispyder.clickcrystalsutils.generators.mobheads;

import io.github.itzispyder.clickcrystalsutils.Generator;
import io.github.itzispyder.clickcrystalsutils.util.nonstatic.Version;

import java.util.Map;
import java.util.Scanner;

public class EntityTextureGenerator implements Generator {

    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        System.out.print("Enter Minecraft version: ");
        String minecraftVersion = scan.nextLine();

        EntityTextureGenerator generator = new EntityTextureGenerator(minecraftVersion, GenType.RAW);
        generator.generateAndCopy();
    }

    private final EntityTextures textures;
    private final GenType genType;

    public enum GenType { RAW, DEFAULT, CODE }

    public EntityTextureGenerator(String minecraftVersion, GenType genType) {
        this.genType = genType;
        this.textures = new EntityTextures(Version.ofString(minecraftVersion));
    }


    @Override
    public String generate() {
        return generateRaw();
    }

    public String generateRaw() {
        textures.reload();

        StringBuilder builder = new StringBuilder();
        builder.append("""
                    | # | **Entity** | **ID** | **Icon** | **Texture** |
                    |:--|:-----------|:-------|:---------|:------------|
                    
                    """.trim());

        int ordinal = 0;
        for (Map.Entry<EntityTextures.EntityKey, String> entry : textures.getTextureData().entrySet())
            builder.append("| %s | %s | %s | ![icon.%s](%s) | %s |\n".formatted(
                    ++ordinal,
                    entry.getKey().displayName(),
                    entry.getKey().id(),
                    entry.getKey().id(),
                    textures.getTexturePaths().get(entry.getKey()),
                    entry.getValue()));

        return builder.toString();
    }
}
