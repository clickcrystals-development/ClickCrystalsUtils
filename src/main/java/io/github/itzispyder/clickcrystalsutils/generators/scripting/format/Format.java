package io.github.itzispyder.clickcrystalsutils.generators.scripting.format;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Format {

    private final List<FormatGroup> groups;

    public Format() {
        this.groups = new ArrayList<>();
    }

    public List<String> parseGroups(String input) {
        Pattern pattern = Pattern.compile(getAcceptingRegex());
        Matcher matcher = pattern.matcher(input);
        List<String> results = new ArrayList<>();

        while (matcher.find())
            results.add(matcher.group());
        return results;
    }

    public Format append(FormatGroup group) {
        groups.add(group);
        return this;
    }

    public boolean has(FormatGroup target) {
        String targetRegex = target.getAcceptingRegex();
        for (FormatGroup group: new ArrayList<>(groups))
            if (group.getAcceptingRegex().equals(targetRegex))
                return true;
        return false;
    }

    public FormatGroup.FormatGroupBuilder append() {
        return new FormatGroup.FormatGroupBuilder() {
            @Override
            public FormatGroup build() {
                FormatGroup group = super.build();
                Format.this.append(group);
                return group;
            }
        };
    }

    public List<FormatGroup> getCodeBlockOpeners() {
        return new ArrayList<>(groups.stream().filter(FormatGroup::isAcceptingCodeBlocks).toList());
    }

    public int size() {
        return groups.size();
    }

    public boolean isEmpty() {
        return groups.isEmpty();
    }

    public void clear() {
        groups.clear();
    }

    public String getAcceptingRegex() {
        return "(" + String.join("|", groups.stream().map(FormatGroup::getAcceptingRegex).toList()) + ")";
    }
}
