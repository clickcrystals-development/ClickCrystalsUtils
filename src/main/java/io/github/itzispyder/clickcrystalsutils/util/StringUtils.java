package io.github.itzispyder.clickcrystalsutils.util;

public class StringUtils {

    public static String snake2pascalCase(String s) {
        StringBuilder builder = new StringBuilder();
        for (String section : s.trim().split("[ _-]"))
            builder.append(capitalize(section));
        return builder.toString();
    }

    public static String capitalize(String s) {
        int len = s.length();
        if (len == 1)
            return s.toUpperCase();
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    public static String capitalizeWords(String s) {
        s = s.replaceAll("[_-]"," ");
        String[] sArray = s.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String str : sArray) sb.append(capitalize(str)).append(" ");
        return sb.toString().trim();
    }
}
