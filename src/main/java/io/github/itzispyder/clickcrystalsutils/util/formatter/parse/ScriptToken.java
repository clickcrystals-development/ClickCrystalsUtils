package io.github.itzispyder.clickcrystalsutils.util.formatter.parse;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// value { children... }
public class ScriptToken {

    public ScriptToken parent;
    public String value;
    public final List<ScriptToken> children = new ArrayList<>();
    public int nestCount;

    public String formatDefault() {
        boolean isParent = !children.isEmpty();
        String indent = "   ".repeat(nestCount);
        StringBuilder builder = new StringBuilder(indent);

        if (value != null) {
            builder.append(value.trim());
            if (isParent)
                builder.append(' ');
        }
        if (isParent) {
            if (value != null)
                builder.append('{');
            builder.append('\n');
            for (ScriptToken child : children)
                if ((child.value != null && !child.value.isBlank()) || !child.children.isEmpty())
                    builder.append(child.formatDefault()).append('\n');
            if (value != null)
                builder.append(indent).append('}');
        }
        return builder.toString();
    }

    public String formatExpanded() {
        boolean isParent = !children.isEmpty();
        String indent = "   ".repeat(nestCount);
        StringBuilder builder = new StringBuilder(indent);

        if (isParent) {
            if (value != null)
                builder.append(value.trim()).append(' ').append('{');
            builder.append('\n');
            for (ScriptToken child : children)
                if ((child.value != null && !child.value.isBlank()) || !child.children.isEmpty())
                    builder.append(child.formatExpanded()).append('\n');
            if (value != null)
                builder.append(indent).append('}');
        }
        else if (value != null) {
            Matcher commandMatcher = ScriptBeautifyStrategy.getCommandPattern().matcher(value);
            Pattern openerPattern = ScriptBeautifyStrategy.getCodeBlockOpenerPattern();
            int subNests = 0;
            while (commandMatcher.find()) {
                String command = commandMatcher.group();
                Matcher openerMatcher = openerPattern.matcher(command);
                if (!openerMatcher.find()) {
                    builder.append(command).append(' ');
                    continue;
                }
                builder.append(command).append(" {\n").append("   ".repeat(nestCount + (++subNests)));
            }
            for (int subNest = subNests - 1; subNest >= 0; subNest--)
                builder.append('\n').append("   ".repeat(nestCount + subNest)).append('}');
        }
        return builder.toString();
    }

    @Override
    public String toString() {
        return formatExpanded();
    }
}
