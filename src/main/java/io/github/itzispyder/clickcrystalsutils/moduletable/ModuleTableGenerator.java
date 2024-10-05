package io.github.itzispyder.clickcrystalsutils.moduletable;

import io.github.itzispyder.clickcrystalsutils.Generator;
import io.github.itzispyder.clickcrystalsutils.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModuleTableGenerator implements Generator {

    public static final String[] categories = {
            "anchoring",
            "clickcrystals",
            "crystalling",
            "misc",
            "optimization",
            "rendering"
    };

    public ModuleTableGenerator() {

    }

    @Override
    public String generate() {
        StringBuilder result = new StringBuilder();

        result.append("| **Module** | **Description** |\n");
        result.append("|:----------:|:---------------:|\n");
        readFiles(result);

        return result.toString();
    }

    public static void readFiles(StringBuilder builder) {
        try {
            String path = "src/main/java/io/github/itzispyder/clickcrystals/modules/modules/";

            for (String cat : categories) {
                String subPath = path + cat;
                File file = new File(subPath);
                File[] subFiles = file.listFiles();

                if (subFiles == null)
                    continue;

                String info;
                for (File javaFile : subFiles)
                    if ((info = readFileToRow(javaFile)) != null)
                        builder.append(info);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static String readFileToRow(File file) throws Exception {
        FileInputStream fis = new FileInputStream(file);
        String contents = new String(fis.readAllBytes());
        fis.close();

        Pattern regex = Pattern.compile("\s*super\\(\\\"(.*)\\\"\\s*,\\s*Categories\\.[A-Z]+\\s*,\\s*\\\"(.*)\\\"\\);\s*");
        Matcher match;

        for (String line : contents.lines().toList()) {
            if ((match = regex.matcher(line)).matches()) {
                String name = StringUtils.snake2pascalCase(match.group(1));
                String desc = match.group(2).replaceAll("\\\\\"", "\"");
                return "| %s | %s |%n".formatted(name, desc);
            }
        }
        return null;
    }
}
