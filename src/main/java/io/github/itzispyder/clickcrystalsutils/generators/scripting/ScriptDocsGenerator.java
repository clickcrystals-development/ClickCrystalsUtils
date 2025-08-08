package io.github.itzispyder.clickcrystalsutils.generators.scripting;

import io.github.itzispyder.clickcrystalsutils.Generator;
import io.github.itzispyder.clickcrystalsutils.generators.scripting.format.Format;
import io.github.itzispyder.clickcrystalsutils.generators.scripting.format.parse.FormatParser;

import java.io.File;
import java.util.function.Consumer;

public class ScriptDocsGenerator implements Generator {

    public static final String PATH_SRC = "src/main/java/io/github/itzispyder/clickcrystals/scripting/";
    public static final String PATH_DEST = "assets/scripting/";

    @Override
    public String generate() {
        Format format = new Format();
        this.forEachSourceFile(it -> FormatParser.registerFileComments(format, it));
        return format.getAcceptingRegex();
    }

    private void forEachSourceFile(Consumer<File> fileConsumer) {
        File src = new File(PATH_SRC);
        sourceFileIterator(src, fileConsumer);
    }

    private void sourceFileIterator(File file, Consumer<File> fileConsumer) {
        if (file == null || !file.exists())
            return;

        if (!file.isDirectory()) {
            fileConsumer.accept(file);
            return;
        }

        File[] subFiles = file.listFiles();
        if (subFiles != null)
            for (File subFile : subFiles)
                sourceFileIterator(subFile, fileConsumer);
    }
}
