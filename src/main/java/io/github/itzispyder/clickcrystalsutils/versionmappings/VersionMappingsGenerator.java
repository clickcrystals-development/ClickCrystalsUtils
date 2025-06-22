package io.github.itzispyder.clickcrystalsutils.versionmappings;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.itzispyder.clickcrystalsutils.Generator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VersionMappingsGenerator implements Generator {

    private static final String MIN_VERSION = "1.20";

    private final String FABRIC_MC_VERSIONS;
    private final String CC_VERSION_MAPPINGS;

    public VersionMappingsGenerator() {
        FABRIC_MC_VERSIONS = "https://maven.fabricmc.net/net/fabricmc/yarn/";
        CC_VERSION_MAPPINGS = "https://itzispyder.github.io/clickcrystals/info.json";
    }

    public static void main(String[] args) {
        System.out.println(new VersionMappingsGenerator().generateAndCopy());
    }

    public List<String> fetchMcVersions() {
        try {
            List<String> list = new ArrayList<>();

            URL url = URI.create(FABRIC_MC_VERSIONS).toURL();
            Document doc = Jsoup.parse(url, 0);
            Elements elements = doc.select("pre > a");
            Pattern buildVersionPattern = Pattern.compile("^([\\d\\.]+)\\+build\\.\\d+/");

            for (Element a : elements) {
                String text = a.text();
                Matcher buildVersionMatcher = buildVersionPattern.matcher(text);

                if (!buildVersionMatcher.matches())
                    continue;
                String mcVersion = buildVersionMatcher.group(1);

                if (!list.contains(mcVersion))
                    list.add(mcVersion);
            }
            Collections.reverse(list);
            return list;
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public JsonObject fetchVersionMapping() {
        try {
            URL url = URI.create(CC_VERSION_MAPPINGS).toURL();
            InputStream is = url.openStream();
            InputStreamReader reader = new InputStreamReader(is);

            JsonObject obj = JsonParser.parseReader(reader).getAsJsonObject();

            reader.close();
            is.close();

            return obj.get("versionMappings").getAsJsonObject();
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public String generate() {
        StringBuilder builder = new StringBuilder();
        builder.append("""
                | What You Have | What to get                         |
                |:--------------|:------------------------------------|
                | higher..      | how is that even possible?          |
                """.trim());
        builder.append('\n');

        JsonObject mappings = fetchVersionMapping();
        for (String version: fetchMcVersions()) {
            JsonElement mappedVersion = mappings.get(version);

            if (mappedVersion == null || mappedVersion.isJsonNull())
                builder.append("| ").append(version).append(" | not supported |\n");
            else
                builder.append("| ").append(version).append(" | ").append(mappedVersion.getAsString()).append(" |\n");

            if (MIN_VERSION.equals(version))
                break;
        }

        builder.append("| ..lower       | cry                                 |");
        return builder.toString();
    }
}
