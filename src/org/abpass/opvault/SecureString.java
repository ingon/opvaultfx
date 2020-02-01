package org.abpass.opvault;

import java.util.function.Consumer;
import java.util.function.Function;

// TODO encrypt
// TODO Cleaner
public final class SecureString implements AutoCloseable {
    private final char[] data;
    
    public SecureString(char[] source) {
        this.data = source.clone();
        // TODO
//      Cleaner.create().register(this, () -> this.close());
    }
    
    public SecureString(char[] source, int start, int len) {
        this.data = new char[len];
        System.arraycopy(source, start, data, 0, len);
        // TODO
//      Cleaner.create().register(this, () -> this.close());
    }
    
    public void accept(Consumer<char[]> consumer) {
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
    
    public <T> T apply(Function<char[], T> fn) {
        char[] copy = new char[data.length];
        System.arraycopy(data, 0, copy, 0, data.length);
        try {
            return fn.apply(copy);
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
