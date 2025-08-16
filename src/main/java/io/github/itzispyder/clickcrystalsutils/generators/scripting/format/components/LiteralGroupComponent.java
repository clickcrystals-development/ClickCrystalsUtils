package io.github.itzispyder.clickcrystalsutils.generators.scripting.format.components;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LiteralGroupComponent extends AbstractGroupComponent {

    public LiteralGroupComponent(String literal) {
        super("(" + literal + ")");
    }

    public LiteralGroupComponent() {
        super("(\\S+)");
    }

    public String getLiteral() {
        Matcher matcher = Pattern.compile("\\((\\S+?)\\)").matcher(this.getAcceptingRegex());
        if (!matcher.find())
            throw new IllegalArgumentException("malformed literal group component: " + this.getAcceptingRegex());
        return matcher.group(1);
    }
}
