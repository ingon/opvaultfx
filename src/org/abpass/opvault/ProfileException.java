package org.abpass.opvault;

import java.nio.file.Path;

public abstract class ProfileException extends Exception {
    private static final long serialVersionUID = 1L;
    
    public ProfileException(String format, Object... args) {
        super(String.format(format, args));
    }

    public ProfileException(Throwable th, String format, Object... args) {
        super(String.format(format, args), th);
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

    public static class InvalidPasswordException extends ProfileException {
        private static final long serialVersionUID = 1L;

        public InvalidPasswordException(Throwable th, Path path) {
            super(th, "incorrect password: %s", path.getFileName());
        }
    }    

    public static class ProfileLockedException extends ProfileException {
        private static final long serialVersionUID = 1L;

        public ProfileLockedException(Path path) {
            super("profile locked: %s", path.getFileName());
        }
    }

    public static class ProfileKeysException extends ProfileException {
        private static final long serialVersionUID = 1L;

        public ProfileKeysException(Path path, OPDataException exc) {
            super(exc, "invalid profile keys: %s", path.getFileName());
        }
    }
    
    public static class ProfileAttachmentException extends ProfileException {
        private static final long serialVersionUID = 1L;
        
        public ProfileAttachmentException(Path path) {
            super("could not load attachment: %s", path.getFileName());
        }
    }
}
