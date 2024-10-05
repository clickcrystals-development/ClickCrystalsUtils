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

        result.append("public static final Map<Class<Packet<?>>, String> C2S = new HashMap<>() {{\n");
        for (String packet: requestPackets(C2S))
            result.append("    this.put(%1$s.class, \"%1$s\");\n".formatted(packet));
        result.append("}};");

        result.append("\n\n");

        result.append("public static final Map<Class<Packet<?>>, String> S2C = new HashMap<>() {{\n");
        for (String packet: requestPackets(S2C))
            result.append("    this.put(%1$s.class, \"%1$s\");\n".formatted(packet));
        result.append("}};");

        return result.toString();
    }

    public List<String> requestPackets(String url) {
        try {
            List<String> result = new ArrayList<>();
            URL link = URI.create(url).toURL();
            Document doc = Jsoup.parse(link, 0);

            Elements columns = doc.select("div.col-first");

            for (Element column : columns) {
                Element packetName = column.select("a").first();
                if (packetName == null)
                    continue;
                String name = packetName.text();
                result.add(name);
            }

            return result;
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return new ArrayList<>();
        }
    }
}
