package io.github.itzispyder.clickcrystalsutils.generators.scripting;

import com.google.gson.GsonBuilder;
import io.github.itzispyder.clickcrystalsutils.Generator;
import io.github.itzispyder.clickcrystalsutils.generators.scripting.docs.Documentation;
import io.github.itzispyder.clickcrystalsutils.generators.scripting.format.Format;
import io.github.itzispyder.clickcrystalsutils.generators.scripting.format.parse.FormatParser;

import java.io.File;
import java.util.function.Consumer;

public class ScriptDocsGenerator implements Generator {

    public static final String PATH_SRC = "src/main/java/io/github/itzispyder/clickcrystals/scripting/";
    public static final String PATH_DEST = "assets/scripting/";
    private final Mode mode;

    public ScriptDocsGenerator(Mode mode) {
        this.mode = mode;
    }

    @Override
    public String generate() {
        Format format = new Format();
        this.forEachSourceFile(it -> FormatParser.registerFileComments(format, it));
        String content;

        switch (mode) {
            case JSON -> content = new GsonBuilder().serializeNulls().setPrettyPrinting().create().toJson(format);
            case REGEX -> content = format.getAcceptingRegex();
            case FILES -> content = generateDocumentationFolder(format);
            default -> content = "null";
        }
        return content;
    }

    public String generateDocumentationFolder(Format format) {
        Documentation documentation = new Documentation();
        this.forEachSourceFile(it -> FormatParser.readFileComments(it).forEach(documentation::registerComment));
        documentation.generateFiles(format);
        return "";
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

    public enum Mode {
        JSON,
        REGEX,
        FILES
    }
}
