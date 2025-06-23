package io.github.itzispyder.clickcrystalsutils;

import io.github.itzispyder.clickcrystalsutils.generators.mobheads.MobHeadGenerator;
import io.github.itzispyder.clickcrystalsutils.generators.moduletable.ModuleTableGenerator;
import io.github.itzispyder.clickcrystalsutils.generators.packetlist.PacketListGenerator;
import io.github.itzispyder.clickcrystalsutils.generators.versionmappings.VersionMappingsGenerator;

public class Main {

    public static void main(String[] args) {
        _assert(args.length >= 1, "Please provide an operation name: ex. -module-table");

        String operation = args[0];
        Generator gen;

        switch (operation) {
            case "module-table", "modules" -> gen = new ModuleTableGenerator();
            case "versions", "version-mappings" -> gen = new VersionMappingsGenerator();
            case "packet-code", "packets" -> {
                _assert(args.length >= 2, "Please provide a Minecraft version! ex. -1.21");
                gen = new PacketListGenerator(args[1].substring(1), false);
            }
            case "packet-table" -> {
                _assert(args.length >= 2, "Please provide a Minecraft version! ex. -1.21");
                gen = new PacketListGenerator(args[1].substring(1), true);
            }
            case "mob-heads", "mob-textures", "entity-textures", "entity-heads" -> {
                _assert(args.length >= 2, "Please provide a Minecraft version! ex. -1.21");
                gen = new MobHeadGenerator(args[1].substring(1), args.length >= 3 && "-raw".equals(args[2]));
            }
            default -> gen = null;
        }

        System.out.println(gen.generateAndCopy());
    }

    public static String getProgramDir() {
        return System.getProperties().getProperty("user.dir");
    }

    public static boolean isInFolder(String folderName) {
        return getProgramDir().replaceAll(".*[\\\\/]", "").trim().equals(folderName);
    }

    public static void _assert(boolean bl, String exception, Object... args) {
        if (!bl)
            throw new IllegalArgumentException(exception.formatted(args));
    }
}