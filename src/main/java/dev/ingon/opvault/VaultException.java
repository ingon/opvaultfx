package dev.ingon.opvault;

import java.nio.file.Path;

public abstract class VaultException extends Exception {
    private static final long serialVersionUID = 1L;
    
    public VaultException(String format, Object... args) {
        super(String.format(format, args));
    }

    public VaultException(Throwable th, String format, Object... args) {
        super(String.format(format, args), th);
    }

    public static class VaultNotFoundException extends VaultException {
        private static final long serialVersionUID = 1L;

        public VaultNotFoundException(Path path) {
            super("path not found: %s", path);
        }
    }
    
    public static class VaultNotDirectoryException extends VaultException {
        private static final long serialVersionUID = 1L;

        public VaultNotDirectoryException(Path path) {
            super("path not directory: %s", path);
        }
    }
    
    public static class VaultProfilesException extends VaultException {
        private static final long serialVersionUID = 1L;
        
        public VaultProfilesException(Path path, Exception exc) {
            super(exc, "could not list profiles: %s", path);
        }
    }
}
