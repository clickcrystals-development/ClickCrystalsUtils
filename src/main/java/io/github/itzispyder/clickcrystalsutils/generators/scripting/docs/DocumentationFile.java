package io.github.itzispyder.clickcrystalsutils.generators.scripting.docs;

import io.github.itzispyder.clickcrystalsutils.generators.scripting.format.Format;
import io.github.itzispyder.clickcrystalsutils.generators.scripting.format.FormatGroup;
import io.github.itzispyder.clickcrystalsutils.generators.scripting.format.components.GroupComponent;
import io.github.itzispyder.clickcrystalsutils.generators.scripting.format.components.MultiLiteralGroupComponent;
import io.github.itzispyder.clickcrystalsutils.generators.scripting.format.parse.FormatParser;
import io.github.itzispyder.clickcrystalsutils.util.FileValidationUtils;
import io.github.itzispyder.clickcrystalsutils.util.StringFormatter;
import io.github.itzispyder.clickcrystalsutils.util.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DocumentationFile {

    private final List<String> rawComments;
    private String name, rawName, regex, path;
    private final List<String> usages;
    private final Format format;

    public DocumentationFile() {
        this.rawComments = new ArrayList<>();
        this.usages = new ArrayList<>();
        this.format = new Format();
    }

    public void generateContents() {
        File file = new File(path);
        if (file.exists()) {
            System.out.printf("<- file '%s' already exists, overwriting%n", path);
//            return;
        }
        if (!FileValidationUtils.validate(file)) {
            System.out.printf("<- file creation failed for '%s'%n", path);
            return;
        }

        StringFormatter formatter = new StringFormatter();
        formatter.def("name", name);
        formatter.def("rawName", rawName);
        formatter.def("regex", regex);
        formatter.def("usages", String.join("\n", usages));
        formatter.def("comments", "# " + String.join("\n# ", rawComments));

        String contents = formatter.format("""
                [<-- Back to Legend](../legend.md)
                
                # Command Name: ${name}
                Keyword: ${rawName}
                
                ### Usages
                ```
                ${usages}
                ```
                
                ### Regex
                ```regexp
                ${regex}
                ```
                
                ### Raw Documentation
                ```yml
                ${comments}
                ```
                """);

        System.out.printf("<- creating file '%s'%n", path);
        FileValidationUtils.quickWrite(file, contents);
    }

    public void registerComment(String rawComment) {
        if (rawComments.contains(rawComment))
            return;

        if (rawName == null) {
            rawName = rawComment.split("\\s+")[0];
            name = StringUtils.capitalizeWords(rawName);
            path = "DOCUMENTATION/commands/%s.md".formatted(rawName);
            System.out.printf("=> Initializing documentation '%s'%n", name);
        }
        else if (!rawComment.startsWith(rawName)) {
            throw new IllegalArgumentException("not a '%s' format: '%s'".formatted(rawName, rawComment));
        }

        rawComments.add(rawComment);

        String[] arguments = rawComment.split("\\s+");
        GroupComponent[] components = FormatParser.parse(rawComment);
        FormatGroup.FormatGroupBuilder builder = format.append();
        Arrays.stream(components).forEach(builder::addComponent);
        builder.build();

        regex = format.getAcceptingRegex();

        this.registerUsageInternal(new StringBuilder(), arguments, components, 0, usages);
    }

    private void registerUsageInternal(StringBuilder resultBuilder, String[] arguments, GroupComponent[] components, int startingIndex, List<String> results) {
        for (int i = startingIndex; i < components.length; i++) {
            GroupComponent component = components[i];
            String argument = arguments[i];

            if (component instanceof MultiLiteralGroupComponent literals) {
                for (String literal : literals.getLiterals()) {
                    StringBuilder branchBuilder = new StringBuilder(resultBuilder);
                    branchBuilder.append(literal).append(' ');
                    this.registerUsageInternal(branchBuilder, arguments, components, i + 1, results);
                }
                return;
            }
            else {
                resultBuilder.append(argument).append(' ');
            }
        }
        results.add(resultBuilder.toString().trim());
    }

    public String getPath() {
        return path;
    }

    public String getRawName() {
        return rawName;
    }

    public String getName() {
        return name;
    }
}
