package org.abpass.opvault;

public class OPDataException extends Exception {
    private static final long serialVersionUID = 1L;
    
    public OPDataException(String format, Object... args) {
        super(String.format(format, args));
    }

    public OPDataException(Throwable th, String format, Object... args) {
        super(String.format(format, args), th);
    }
}
