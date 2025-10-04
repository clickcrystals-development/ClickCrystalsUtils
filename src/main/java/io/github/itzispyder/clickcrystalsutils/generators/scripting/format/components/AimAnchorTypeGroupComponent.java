package io.github.itzispyder.clickcrystalsutils.generators.scripting.format.components;

import io.github.itzispyder.clickcrystalsutils.util.FileValidationUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AimAnchorTypeGroupComponent extends MultiLiteralGroupComponent {

    private static List<String> list;

    public AimAnchorTypeGroupComponent() {
        super(getList());
    }

    private static List<String> getList() {
        if (list == null) {
            String path = "src/main/java/io/github/itzispyder/clickcrystals/scripting/syntax/AimAnchorType.java";
            File file = new File(path);
            String content = FileValidationUtils.quickRead(file);

            Matcher matcher = Pattern.compile("([A-Z_]+)\\(.*\\)[,;]").matcher(content);
            List<String> names = new ArrayList<>();
            while (matcher.find()) {
                String match = matcher.group(1).toLowerCase();
                names.add(match);
            }
            list = names;
        }
        return list;
    }
}
