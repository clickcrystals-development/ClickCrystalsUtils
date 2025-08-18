package io.github.itzispyder.clickcrystalsutils.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;

public final class FileValidationUtils {

    public static boolean validate(File file) {
        try {
            if (!file.getParentFile().exists())
                if (!file.getParentFile().mkdirs())
                    return false;
            if (!file.exists())
                if (!file.createNewFile())
                    return false;
            return true;
        }
        catch (Exception ex) {
            return false;
        }
    }

    public static boolean quickWrite(File file, String string) {
        if (file == null || !validate(file)) {
            return false;
        }

        try {
            FileWriter fw = new FileWriter(file);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(string);
            bw.flush();
            bw.close();
            fw.close();
            return true;
        }
        catch (Exception ex) {
            return false;
        }
    }

    public static String quickRead(File file) {
        try {
            FileInputStream fis = new FileInputStream(file);
            String content = new String(fis.readAllBytes());
            fis.close();
            return content;
        }
        catch (Exception ex) {
            return "";
        }
    }
}