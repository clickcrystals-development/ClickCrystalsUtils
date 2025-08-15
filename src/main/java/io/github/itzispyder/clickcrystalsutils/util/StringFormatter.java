package io.github.itzispyder.clickcrystalsutils.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringFormatter {

    public static final Pattern VARIABLES_PATTERN = Pattern.compile("\\$\\{(?<name>\\w+)\\}");
    private final Map<String, Object> variables;

    public StringFormatter() {
        this.variables = new HashMap<>();
    }

    public void def(String varName, Object value) {
        variables.put(varName, value);
    }

    public String format(String input) {
        Matcher matcher = VARIABLES_PATTERN.matcher(input);

        while (matcher.find()) {
            String rawVar = matcher.group();
            String var = matcher.group("name");
            input = input.replace(rawVar, String.valueOf(variables.get(var)));
        }

        return input;
    }
}
