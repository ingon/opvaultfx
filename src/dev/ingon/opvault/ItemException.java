package dev.ingon.opvault;

public abstract class ItemException extends Exception {
    private static final long serialVersionUID = 1L;
    
    public ItemException(String format, Object... args) {
        super(String.format(format, args));
    }

    public ItemException(Throwable th, String format, Object... args) {
        super(String.format(format, args), th);
    }

    public static class ItemOverviewKeyException extends ItemException {
        private static final long serialVersionUID = 1L;

        public ItemOverviewKeyException(Exception exc) {
            super(exc, "invalid overview key");
        }
    }
    
    public static class ItemOverviewParseException extends ItemException {
        private static final long serialVersionUID = 1L;

        public ItemOverviewParseException(Exception exc) {
            super(exc, "cannot parse overview");
        }
    }


    public static class ItemDetailKeyException extends ItemException {
        private static final long serialVersionUID = 1L;

        public ItemDetailKeyException(Exception exc) {
            super(exc, "invalid detail key");
        }
    }
    
    public static class ItemDetailParseException extends ItemException {
        private static final long serialVersionUID = 1L;

        public ItemDetailParseException(Exception exc) {
            super(exc, "cannot parse detail");
        }
    }
}
