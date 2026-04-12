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

        EntityTextureGenerator generator = new EntityTextureGenerator(minecraftVersion);
        generator.generateAndCopy();
        System.out.println(generator.generate());
    }

    private final EntityTextures textures;

    public EntityTextureGenerator(String minecraftVersion) {
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
        builder.append('\n');

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
