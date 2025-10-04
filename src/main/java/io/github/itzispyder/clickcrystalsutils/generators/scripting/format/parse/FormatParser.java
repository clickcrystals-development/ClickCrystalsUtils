package io.github.itzispyder.clickcrystalsutils.generators.scripting.format.parse;

import io.github.itzispyder.clickcrystalsutils.generators.scripting.format.Format;
import io.github.itzispyder.clickcrystalsutils.generators.scripting.format.FormatGroup;
import io.github.itzispyder.clickcrystalsutils.generators.scripting.format.components.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// examples:
// @Format on tick <int>?
// @Format on right_click
// @Format on chat_send <quote>

/*
        Documentation Legend:

        <int>               integer
        <num>               number
        <vec>               singular relative vector component              aka <x>,<y>,<z>,<pitch>,<yaw>
        <comparator>        > < == >= <= !=
        <identifier>        :direct_identifier OR #indirect_identifier
        <client-packet>     client packet id
        <server-packet>     server packet id
        <input>             an input type
        <aim-anchor>        aim anchor
        ...                 literal
        "..."               quoted literal
        \w+                 constant literal
        (\w+|\w+|...)       constant literals
        {}                  command line or code block of command lines

        the argument is optional if a ? is appended at the end
 */
public class FormatParser {

    private static final List<ComponentLookup<?>> componentDictionary = new ArrayList<>() {{
        this.add(new ComponentLookup<>("<int>\\??", arg -> ComponentLookup.withOptional(arg, new IntegerGroupComponent())));
        this.add(new ComponentLookup<>("<num>\\??", arg -> ComponentLookup.withOptional(arg, new NumberGroupComponent())));
        this.add(new ComponentLookup<>("<comparator>\\??", arg -> ComponentLookup.withOptional(arg, new ComparatorGroupComponent())));
        this.add(new ComponentLookup<>("<identifier>\\??", arg -> ComponentLookup.withOptional(arg, new IdentifierGroupComponent())));
        this.add(new ComponentLookup<>("<(x|y|z|pitch|yaw|vec)>\\??", arg -> ComponentLookup.withOptional(arg, new VectorValueGroupComponent())));
        this.add(new ComponentLookup<>("\\{\\}\\??", arg -> ComponentLookup.withOptional(arg, new FillerGroupComponent())));
        this.add(new ComponentLookup<>("<server-packet>\\??", arg -> ComponentLookup.withOptional(arg, new NetworkServerPacketGroupComponent())));
        this.add(new ComponentLookup<>("<client-packet>\\??", arg -> ComponentLookup.withOptional(arg, new NetworkClientPacketGroupComponent())));
        this.add(new ComponentLookup<>("<input>\\??", arg -> ComponentLookup.withOptional(arg, new InputTypeGroupComponent())));
        this.add(new ComponentLookup<>("<aim-anchor>\\??", arg -> ComponentLookup.withOptional(arg, new AimAnchorTypeGroupComponent())));

        this.add(new ComponentLookup<>("!?\\w+\\??", arg -> ComponentLookup.withOptional(arg, new LiteralGroupComponent(arg))));
        this.add(new ComponentLookup<>("\\((!?\\w+\\|?)+\\)\\??", arg -> {
            Matcher matcher = Pattern.compile("(!?\\w+)\\|?").matcher(arg);
            List<String> matches = new ArrayList<>();
            while (matcher.find()) {
                matches.add(matcher.group(1));
            }
            return ComponentLookup.withOptional(arg, new MultiLiteralGroupComponent(matches));
        }));
        this.add(new ComponentLookup<>("...\\??", arg -> ComponentLookup.withOptional(arg, new LiteralGroupComponent())));
        this.add(new ComponentLookup<>("\"...\"\\??", arg -> ComponentLookup.withOptional(arg, new QuoteGroupComponent())));
    }};

    public static void register(Format destination, String input) {
        FormatGroup.FormatGroupBuilder builder = destination.append();
        for (GroupComponent component : parse(input))
            builder.addComponent(component);
        if (input.matches("^.*\\{\\}\\??.*$"))
            builder.setAcceptingCodeBlocks(true);
        builder.build();
    }

    public static void registerFileComments(Format destination, File input) {
        for (String comment: readFileComments(input))
            register(destination, comment);
    }

    public static List<String> readFileComments(File input) {
        String regex = "//\\s*@Format\\s+(?<documentation>(\\S+\\s*)+)\\s*";
        Pattern pattern = Pattern.compile(regex);

        try {
            FileReader fr = new FileReader(input);
            BufferedReader br = new BufferedReader(fr);
            List<String> lines = new ArrayList<>();
            String line;

            while ((line = br.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.find())
                    lines.add(matcher.group("documentation"));
            }

            br.close();
            fr.close();
            return lines;
        }
        catch (IOException | FormatParseException ex) {
            if (ex instanceof FormatParseException formatParseException) {
                formatParseException.setFile(input);
                throw formatParseException;
            }
            throw new RuntimeException(ex);
        }
    }

    public static GroupComponent[] parse(String input) {
        try {
            String[] arguments = input.split("\\s+");
            GroupComponent[] components = new GroupComponent[arguments.length];

            for (int i = 0; i < arguments.length; i++) {
                String argument = arguments[i];
                GroupComponent component = parseComponent(argument);
                components[i] = component;
            }
            return components;
        }
        catch (FormatParseException ex) {
            ex.setCapturedGroup(input);
            throw ex;
        }
    }

    private static GroupComponent parseComponent(String argument) {
        try {
            for (ComponentLookup<?> lookup : componentDictionary)
                if (lookup.matches(argument))
                    return lookup.parse(argument);
            throw new IllegalArgumentException("No such component lookup");
        }
        catch (Exception ex) {
            FormatParseException formatParseException = new FormatParseException(ex.getMessage());
            formatParseException.setCapturedComponent(argument);
            throw formatParseException;
        }
    }
}
