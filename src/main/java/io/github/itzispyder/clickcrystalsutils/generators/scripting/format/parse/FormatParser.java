package io.github.itzispyder.clickcrystalsutils.generators.scripting.format.parse;

import io.github.itzispyder.clickcrystalsutils.generators.scripting.format.Format;
import io.github.itzispyder.clickcrystalsutils.generators.scripting.format.FormatGroup;
import io.github.itzispyder.clickcrystalsutils.generators.scripting.format.components.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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
        <comparator>        > < == >= <= !=
        ...                 literal
        "..."               quoted literal
        \w+                 constant literal
        (\w+|\w+|...)       constant literals

        the argument is optional if a ? is appended at the end
 */
public class FormatParser {

    private static final List<ComponentLookup<?>> componentDictionary = new ArrayList<>() {{
        this.add(new ComponentLookup<>("<int>\\??", arg -> ComponentLookup.withOptional(arg, new IntegerGroupComponent())));
        this.add(new ComponentLookup<>("<num>\\??", arg -> ComponentLookup.withOptional(arg, new NumberGroupComponent())));
        this.add(new ComponentLookup<>("...\\??", arg -> ComponentLookup.withOptional(arg, new LiteralGroupComponent())));
        this.add(new ComponentLookup<>("\"...\"\\??", arg -> ComponentLookup.withOptional(arg, new QuoteGroupComponent())));
        this.add(new ComponentLookup<>("\\w+\\??", arg -> ComponentLookup.withOptional(arg, new LiteralGroupComponent(arg))));
        this.add(new ComponentLookup<>("<comparator>\\??", arg -> ComponentLookup.withOptional(arg, new ComparatorGroupComponent())));
        this.add(new ComponentLookup<>("\\((\\w+\\|?)+\\)\\??", arg -> {
            Matcher matcher = Pattern.compile("(\\w+)\\|?").matcher(arg);
            List<String> matches = new ArrayList<>();
            while (matcher.find())
                matches.add(matcher.group(1));
            return ComponentLookup.withOptional(arg, new MultiLiteralGroupComponent(matches));
        }));
    }};

    public static void register(Format destination, String input) {
        FormatGroup.FormatGroupBuilder builder = destination.append();
        for (GroupComponent component : parse(input))
            builder.addComponent(component);
        builder.build();
    }

    public static void registerFileComments(Format destination, File input) {
        String regex = "//\\s*@Format\\s+(?<documentation>(\\S+\\s*)+)\\s*";
        Pattern pattern = Pattern.compile(regex);

        try {
            FileReader fr = new FileReader(input);
            BufferedReader br = new BufferedReader(fr);
            String line;

            while ((line = br.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.find())
                    register(destination, matcher.group("documentation"));
            }

            br.close();
            fr.close();
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static GroupComponent[] parse(String input) {
        String[] arguments = input.split("\\s+");
        GroupComponent[] components = new GroupComponent[arguments.length];

        for (int i = 0; i < arguments.length; i++) {
            String argument = arguments[i];
            GroupComponent component = parseComponent(argument);
            components[i] = component;
        }
        return components;
    }

    private static GroupComponent parseComponent(String argument) {
        for (ComponentLookup<?> lookup : componentDictionary)
            if (lookup.matches(argument))
                return lookup.parse(argument);
        return null;
    }
}
