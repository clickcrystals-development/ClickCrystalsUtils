package io.github.itzispyder.clickcrystalsutils;

import io.github.itzispyder.clickcrystalsutils.moduletable.ModuleTableGenerator;
import io.github.itzispyder.clickcrystalsutils.packetlist.PacketListGenerator;

public class Main {

    public static void main(String[] args) {
        _assert(args.length >= 1, "Please provide an operation name: ex. -modules");

        String operation = args[0];
        Generator gen;

        switch (operation) {
            case "modules" -> gen = new ModuleTableGenerator();
            case "packets" -> {
                _assert(args.length >= 2, "Please provide a Minecraft version! ex. -1.21");
                gen = new PacketListGenerator(args[1].substring(1));
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