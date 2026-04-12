package io.github.itzispyder.clickcrystalsutils.util;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class GithubUtils {

    public static File downloadRepo(String repoUrl) {
        System.out.println("downloading remote repository: " + repoUrl);
        try (var zip = new ZipInputStream(URI.create(repoUrl).toURL().openStream())) {
            File parent = new File("temp-git-repository-download.zip");
            ZipEntry entry;

            parent.mkdirs();
            while ((entry = zip.getNextEntry()) != null) {
                System.out.println("-> unzipping " + entry.getName());
                File child = new File(parent, entry.getName());

                if (entry.isDirectory()) {
                    child.mkdirs();
                }
                else try (FileOutputStream childOut = new FileOutputStream(child)) {
                    childOut.write(zip.readAllBytes());
                }
            }

            System.out.println("<- download complete");
            return parent.listFiles()[0]; // first zip dir because almost always zip files have a root folder
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void deleteRepo(String repoUrl) {
        System.out.println("deleting remote repository: " + repoUrl);
        File file = new File("temp-git-repository-download.zip");
        deepDelete(file);
        System.out.println("<- deletion complete");
    }

    private static void deepDelete(File file) {
        if (!file.isDirectory()) {
            while (!file.delete());
            return;
        }

        File[] children = file.listFiles();
        if (children == null)
            return;

        for (File child : children)
            deepDelete(child);
        while (!file.delete());
    }
}
