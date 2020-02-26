package dev.ingon.opvaultfx;

public class SettingsException extends Exception {
    private static final long serialVersionUID = 1L;

    public SettingsException(String message, Exception exc) {
        super(message, exc);
    }
}
