package org.abpass.opvault;

import java.util.function.Consumer;

// TODO encrypt
// TODO Cleaner
public final class SecureString implements AutoCloseable {
    private final char[] data;
    
    public SecureString(char[] source) {
        this.data = new char[source.length];
        System.arraycopy(source, 0, data, 0, source.length);
    }
    
    public SecureString(char[] source, int start, int len) {
        this.data = new char[len];
        System.arraycopy(source, start, data, 0, len);
    }
    
    public void access(Consumer<char[]> consumer) {
        char[] copy = new char[data.length];
        System.arraycopy(data, 0, copy, 0, data.length);
        try {
            consumer.accept(copy);
        } finally {
            for (int i = 0, n = copy.length; i < n; i++) {
                copy[i] = '\0';
            }
        }
    }

    @Override
    public void close() throws Exception {
        for (int i = 0, n = data.length; i < n; i++) {
            data[i] = '\0';
        }
    }
    
    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }
}
