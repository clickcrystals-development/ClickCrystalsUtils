package io.github.itzispyder.clickcrystalsutils.generators.versionmappings;

import com.google.gson.JsonArray;
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

    private static final String MIN_VERSION = "1.21";

    private final String FABRIC_MC_VERSIONS, CC_VERSION_MAPPINGS, GITHUB_RELEASES;

    private String latestVersion;

    public VersionMappingsGenerator() {
        FABRIC_MC_VERSIONS = "https://maven.fabricmc.net/net/fabricmc/yarn/";
        CC_VERSION_MAPPINGS = "https://itzispyder.github.io/clickcrystals/info.json";
        GITHUB_RELEASES = "https://api.github.com/repos/clickcrystals-development/ClickCrystals/releases?per_page=100";
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

    public String fetchLatestMcVersion() {
        return fetchMcVersions().get(0);
    }

    public JsonObject fetchVersionMapping() {
        try {
            URL url = URI.create(CC_VERSION_MAPPINGS).toURL();
            InputStream is = url.openStream();
            InputStreamReader reader = new InputStreamReader(is);

            JsonObject obj = JsonParser.parseReader(reader).getAsJsonObject();

            reader.close();
            is.close();

            latestVersion = obj.get("latest").getAsString();
            return obj.get("versionMappings").getAsJsonObject();
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private List<String> fetchGithubAssets(int pageNumber) {
        try {
            List<String> list = new ArrayList<>();

            URL url = URI.create("%s&page=%s".formatted(GITHUB_RELEASES, pageNumber)).toURL();
            InputStream is = url.openStream();
            InputStreamReader reader = new InputStreamReader(is);

            JsonArray arr = JsonParser.parseReader(reader).getAsJsonArray();

            reader.close();
            is.close();

            boolean fetchAgain = arr.size() == 100;

            for (var i = 0; i < arr.size(); i++) {
                JsonObject release = arr.get(i).getAsJsonObject();
                JsonArray assets = release.get("assets").getAsJsonArray();
                String releaseUri = release.get("html_url").getAsString();

                for (JsonElement asset : assets) {
                    String assetName = asset.getAsJsonObject().get("name").getAsString();
                    list.add("[%s](%s)".formatted(assetName, releaseUri));
                }
            }

            if (fetchAgain)
                list.addAll(fetchGithubAssets(pageNumber + 1));
            return list;
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

        System.out.println("fetching githubAssets...");
        List<String> allAssets = fetchGithubAssets(1);

        System.out.println("fetching versionMappings...");
        JsonObject mappings = fetchVersionMapping();

        System.out.println("fetching fabricMinecraftVersions...");
        System.out.println();

        for (String version: fetchMcVersions()) {
            JsonElement mappedVersion = mappings.get(version);

            if (mappedVersion == null || mappedVersion.isJsonNull())
                builder.append("| ").append(version).append(" | not supported |\n");
            else {
                String asset = "ClickCrystals-%s-(latestVersion).jar".formatted(mappedVersion);
                Pattern assetPattern = Pattern.compile("\\[ClickCrystals-([\\d.]+)-([\\d.]+)\\.jar\\]\\(.*\\)");

                for (String name : allAssets) {
                    Matcher assetMatcher = assetPattern.matcher(name);
                    if (!assetMatcher.matches()) {
                        System.out.println("INVALID ASSET FORMAT -> " + name);
                        continue;
                    }

                    String assetMcVer = assetMatcher.group(1);
                    if (assetMcVer.equals(mappedVersion.getAsString())) {
                        asset = name;
                        break;
                    }
                }

                if (asset.contains(latestVersion))
                    asset += " *recommended";

                builder.append("| %s | %s |\n".formatted(version, asset));
            }

            if (MIN_VERSION.equals(version))
                break;
        }

        builder.append("| ..lower       | cry                                 |");
        return builder.toString();
    }
}
