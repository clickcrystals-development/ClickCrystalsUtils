package io.github.itzispyder.clickcrystalsutils.generators.scripting.format.components;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MultiLiteralGroupComponent extends AbstractGroupComponent {

    public MultiLiteralGroupComponent(Iterable<String> literals) {
        super("(" + String.join("|", literals) + ")");
    }

    public List<String> getLiterals() {
        Matcher matcher = Pattern.compile("(!?\\w+)\\|?").matcher(this.getAcceptingRegex());
        List<String> matches = new ArrayList<>();
        while (matcher.find())
            matches.add(matcher.group(1));
        return matches;
    }
}
