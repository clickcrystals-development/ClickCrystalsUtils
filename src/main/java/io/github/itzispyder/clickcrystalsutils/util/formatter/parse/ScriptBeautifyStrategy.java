package io.github.itzispyder.clickcrystalsutils.util.formatter.parse;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.regex.Pattern;

public enum ScriptBeautifyStrategy {

    DEFAULT,
    EXPANDED,
    SINGLE_LINE;

    private static Pattern commandPattern, codeBlockOpenerPattern;

    public static Pattern getCodeBlockOpenerPattern() {
        cachePatternsIfNecessary();
        return codeBlockOpenerPattern;
    }

    public static Pattern getCommandPattern() {
        cachePatternsIfNecessary();
        return commandPattern;
    }

    private static void cachePatternsIfNecessary() {
        if (commandPattern != null && codeBlockOpenerPattern != null)
            return;

        String commandRegex = fetch("https://raw.githubusercontent.com/clickcrystals-development/ClickCrystalsScripting/refs/heads/master/DOCUMENTATION/regex.txt");
        String codeBlockOpenerRegex = fetch("https://raw.githubusercontent.com/clickcrystals-development/ClickCrystalsScripting/refs/heads/master/DOCUMENTATION/code_block_openers.txt");
        commandPattern = Pattern.compile(commandRegex);
        codeBlockOpenerPattern = Pattern.compile(codeBlockOpenerRegex);
    }

    private static String fetch(String url) {
        try {
            URL link = URI.create(url).toURL();
            InputStream in = link.openStream();
            String content = new String(in.readAllBytes());
            in.close();
            return content;
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
