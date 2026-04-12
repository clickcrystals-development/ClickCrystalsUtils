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

    public static int editDistance(String a, String b) {
        int m = a.length();
        int n = b.length();
        int[][] mat = new int[m + 1][n + 1];

        for (int i = 0; i <= m; i++)
            mat[i][0] = i;
        for (int i = 0; i <= n; i++)
            mat[0][i] = i;

        for (int i = 1; i <= m; i++) for (int j = 1; j <= n; j++) {
            if (a.charAt(i - 1) == b.charAt(j - 1))
                mat[i][j] = mat[i - 1][j - 1];
            else
                mat[i][j] = 1 + Math.min(mat[i - 1][j],
                                Math.min(mat[i][j - 1],
                                         mat[i - 1][j - 1]));
        }
        return mat[m][n];
    }

    public static float similarity(String a, String b) {
        float dist = editDistance(a, b);
        float max = Math.max(a.length(), b.length());
        float min = Math.min(a.length(), b.length());

        if (a.contains(b) || b.contains(a)) { // so bats won't match cats lmao
            return .8F + .2F * (min / max);
        }

        return 1 - (dist / max);
    }
}
