package io.github.itzispyder.clickcrystalsutils;

import java.awt.*;
import java.awt.datatransfer.StringSelection;

public interface Generator {

    String generate();

    default String generateAndCopy() {
        String generated = this.generate();
        StringSelection data = new StringSelection(generated);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(data, null);
        return generated;
    }

    default void assertInFolder(String folderName) {
        if (!Main.isInFolder(folderName))
            throw new IllegalArgumentException("Program is expected to be running in the folder directly '%s', but got '%s'".formatted(folderName, Main.getProgramDir()));
    }
}
