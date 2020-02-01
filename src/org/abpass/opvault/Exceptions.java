package org.abpass.opvault;

import java.nio.file.Path;

public class Exceptions {
    private static abstract class FormattedException extends Exception {
        private static final long serialVersionUID = 1L;
        
        public FormattedException(String format, Object... args) {
            super(String.format(format, args));
        }

        public FormattedException(Throwable th, String format, Object... args) {
            super(String.format(format, args), th);
        }
    }
    
    public static abstract class VaultException extends FormattedException {
        private static final long serialVersionUID = 1L;
        
        public VaultException(String format, Object... args) {
            super(format, args);
        }

        public VaultException(Throwable th, String format, Object... args) {
            super(th, format, args);
        }
    }

    public static abstract class ProfileException extends FormattedException {
        private static final long serialVersionUID = 1L;
        
        public ProfileException(String format, Object... args) {
            super(format, args);
        }

        public ProfileException(Throwable th, String format, Object... args) {
            super(th, format, args);
        }
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
    
    public static class ProfileNotFoundException extends ProfileException {
        private static final long serialVersionUID = 1L;

        public ProfileNotFoundException(Path path) {
            super("profile %s not found at: %s", path.getParent().getFileName(), path);
        }
    }
    
    public static class ProfileNotFileException extends ProfileException {
        private static final long serialVersionUID = 1L;

        public ProfileNotFileException(Path path) {
            super("profile %s is not a file", path.getParent().getFileName());
        }
    }

    public static class ProfileFormatException extends ProfileException {
        private static final long serialVersionUID = 1L;

        public ProfileFormatException(Path path) {
            super("profile %s incorrect format", path.getParent().getFileName());
        }
    }
    
    public static class ProfileReadException extends ProfileException {
        private static final long serialVersionUID = 1L;

        public ProfileReadException(Path path, Exception exc) {
            super(exc, "profile %s cannot be read: %s", path.getParent().getFileName(), exc.getMessage());
        }
    }

    public static class ProfileBandFormatException extends ProfileException {
        private static final long serialVersionUID = 1L;

        public ProfileBandFormatException(Path path) {
            super("profile %s band incorrect format", path.getParent().getFileName());
        }
    }

    public static class ProfileBandReadException extends ProfileException {
        private static final long serialVersionUID = 1L;
        
        public ProfileBandReadException(Path path, Exception exc) {
            super(exc, "profile %s band cannot be read", path.getParent().getFileName());
        }
    }
    
    public static class InvalidPasswordException extends FormattedException {
        private static final long serialVersionUID = 1L;

        public InvalidPasswordException(Throwable th, Path path) {
            super(th, "incorrect password for %s", path.getFileName());
        }
    }
    
    public static class InvalidOpdataException extends FormattedException {
        private static final long serialVersionUID = 1L;

        public InvalidOpdataException(String format, Object... args) {
            super(format, args);
        }
        
        public InvalidOpdataException(Throwable th, String format, Object... args) {
            super(String.format(format, args), th);
        }
    }
}
