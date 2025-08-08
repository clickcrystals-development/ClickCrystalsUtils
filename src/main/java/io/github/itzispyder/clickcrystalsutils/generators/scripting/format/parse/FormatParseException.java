package io.github.itzispyder.clickcrystalsutils.generators.scripting.format.parse;

import java.io.File;

public class FormatParseException extends RuntimeException {

    private String capturedComponent, capturedGroup, file;

    public FormatParseException(String message) {
        super(message);
    }

    @Override
    public String getMessage() {
        String exception = super.getMessage();
        String message = "Failed to parse documented format";

        if (capturedComponent != null)
            message += " component " + capturedComponent;
        if (capturedGroup != null)
            message += " in group " + capturedGroup;
        if (file != null)
            message += " from file " + file;

        return message + ": " + exception;
    }

    public void setCapturedComponent(String capturedComponent) {
        this.capturedComponent = capturedComponent;
    }

    public void setCapturedGroup(String capturedGroup) {
        this.capturedGroup = capturedGroup;
    }

    public void setFile(File file) {
        this.file = file.getPath();
    }
}
