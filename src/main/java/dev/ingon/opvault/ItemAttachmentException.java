package dev.ingon.opvault;

import java.nio.file.Path;

public abstract class ItemAttachmentException extends Exception {
    private static final long serialVersionUID = 1L;
    
    public ItemAttachmentException(String format, Object... args) {
        super(String.format(format, args));
    }

    public ItemAttachmentException(Throwable th, String format, Object... args) {
        super(String.format(format, args), th);
    }

    public static class ItemAttachmentInvalidHeaderException extends ItemAttachmentException {
        private static final long serialVersionUID = 1L;

        public ItemAttachmentInvalidHeaderException(Path path) {
            super("invalid attachment header for: %s", path);
        }
    }

    public static class ItemAttachmentInvalidMetadataException extends ItemAttachmentException {
        private static final long serialVersionUID = 1L;

        public ItemAttachmentInvalidMetadataException(Path path, Exception exc) {
            super(exc, "invalid attachment metadata for: %s", path);
        }
    }
    
    public static class ItemAttachmentOverviewException extends ItemAttachmentException {
        private static final long serialVersionUID = 1L;

        public ItemAttachmentOverviewException(Exception exc) {
            super(exc, "cannot load overview");
        }
    }

    public static class ItemAttachmentOverviewParseException extends ItemAttachmentException {
        private static final long serialVersionUID = 1L;
        
        public ItemAttachmentOverviewParseException(Exception exc) {
            super(exc, "cannot parse overview");
        }
    }

    public static class ItemAttachmentIconException extends ItemAttachmentException {
        private static final long serialVersionUID = 1L;

        public ItemAttachmentIconException(Exception exc) {
            super(exc, "cannot load icon");
        }
    }

    public static class ItemAttachmentDataException extends ItemAttachmentException {
        private static final long serialVersionUID = 1L;

        public ItemAttachmentDataException(Exception exc) {
            super(exc, "cannot load data");
        }
    }
}
