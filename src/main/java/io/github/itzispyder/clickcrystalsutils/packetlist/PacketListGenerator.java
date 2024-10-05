package io.github.itzispyder.clickcrystalsutils.packetlist;

import io.github.itzispyder.clickcrystalsutils.Generator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class PacketListGenerator implements Generator {

    private final String C2S, S2C;

    public PacketListGenerator(String minecraftVersion) {
        this.C2S = "https://maven.fabricmc.net/docs/yarn-%s+build.1/net/minecraft/network/packet/c2s/play/package-summary.html".formatted(minecraftVersion);
        this.S2C = "https://maven.fabricmc.net/docs/yarn-%s+build.1/net/minecraft/network/packet/s2c/play/package-summary.html".formatted(minecraftVersion);
    }

    @Override
    public String generate() {
        StringBuilder result = new StringBuilder();

        result.append("package io.github.itzispyder.clickcrystals.client.networking;\n\n");

        result.append("import net.minecraft.network.packet.Packet;\n");
        result.append("import net.minecraft.network.packet.c2s.play.*;\n");
        result.append("import net.minecraft.network.packet.s2c.play.*;\n\n");

        result.append("import java.util.HashMap;\n");
        result.append("import java.util.Map;\n\n");

        result.append("public class PacketMapper {\n\n");

        result.append("    public static final Map<Class<? extends Packet<?>>, Info> C2S = new HashMap<>() {{\n");
        for (Info packet: requestPackets(C2S))
            result.append("        this.put(%2$s.class, new Info(\"%1$s\", \"%2$s\"));\n".formatted(packet.id, packet.className));
        result.append("    }};");

        result.append("\n\n");

        result.append("    public static final Map<Class<? extends Packet<?>>, Info> S2C = new HashMap<>() {{\n");
        for (Info packet: requestPackets(S2C))
            result.append("        this.put(%2$s.class, new Info(\"%1$s\", \"%2$s\"));\n".formatted(packet.id, packet.className));
        result.append("    }};\n\n");

        result.append("    public record Info(String id, String className) {}\n");

        result.append("}\n");

        return result.toString();
    }

    public List<Info> requestPackets(String url) {
        try {
            List<Info> result = new ArrayList<>();
            URL link = URI.create(url).toURL();
            Document doc = Jsoup.parse(link, 0);

            Elements columns = doc.select("div.col-first");

            for (Element column : columns) {
                Element packetName = column.select("a").first();
                if (packetName == null)
                    continue;
                String name = packetName.text();
                if (!name.matches("^.*(S2C|C2S)Packet$"))
                    continue;
                result.add(new Info(name));
            }

            return result;
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return new ArrayList<>();
        }
    }

    public record Info(String id, String className) {
        public Info(String className) {
            this(className.substring(0, 1).toLowerCase()
                    + className.substring(1).replaceAll("(S2C|C2S)Packet", ""),
                    className);
        }
    }
}
