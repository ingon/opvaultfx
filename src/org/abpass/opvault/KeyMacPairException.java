package org.abpass.opvault;

public class KeyMacPairException extends Exception {
    private static final long serialVersionUID = 1L;
    
    public KeyMacPairException(String format, Object... args) {
        super(String.format(format, args));
    }

    public KeyMacPairException(Throwable th, String format, Object... args) {
        super(String.format(format, args), th);
    }
}
