package org.abpass.opvault;

import java.lang.ref.Cleaner;
import java.lang.ref.Cleaner.Cleanable;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public final class SecureString implements AutoCloseable {
    private static final Cleaner cleaner = Cleaner.create();
    
    private final Cleanable cleanable;
    
    private final SecretKey key;
    private final byte[] data;
    
    public SecureString() {
        this(new char[0]);
    }
    
    public SecureString(char[] source) {
        SecretKey key = generateKey();
        
        byte[] primData = Charset.defaultCharset().encode(CharBuffer.wrap(source)).array();
        byte[] data;
        
        try {
            Cipher cipher = Security.getAESPadding();
            cipher.init(Cipher.ENCRYPT_MODE, key);
            data = cipher.doFinal(primData);
        } catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            throw new RuntimeException("cannot encrypt", e);
        } finally {
            Security.wipe(primData);
        }
        
        this.cleanable = cleaner.register(this, () -> Security.wipe(data));
        
        this.key = key;
        this.data = data;
    }
    
    public SecureString(char[] source, int start, int len) {
        SecretKey key = generateKey();

        byte[] primData = Charset.defaultCharset().encode(CharBuffer.wrap(source, start, len)).array();
        byte[] data;
        
        try {
            Cipher cipher = Security.getAESPadding();
            cipher.init(Cipher.ENCRYPT_MODE, key);
            data = cipher.doFinal(primData);
        } catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            throw new RuntimeException("cannot encrypt", e);
        } finally {
            Security.wipe(primData);
        }
        
        this.cleanable = cleaner.register(this, () -> Security.wipe(data));
        
        this.key = key;
        this.data = data;
    }
    
    private static SecretKey generateKey() {
        try {
            KeyGenerator generator = KeyGenerator.getInstance("AES");
            generator.init(128);
            return generator.generateKey();
        } catch(NoSuchAlgorithmException exc) {
            throw new Error(exc);
        }
    }

    public SecureString append(char[] added) {
        try {
            Cipher cipher = Security.getAESPadding();
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] plainData = cipher.doFinal(data);
            char[] chars = Charset.defaultCharset().decode(ByteBuffer.wrap(plainData)).array();
            
            char[] newChars = new char[chars.length + added.length];
            System.arraycopy(chars, 0, newChars, 0, chars.length);
            System.arraycopy(added, 0, newChars, chars.length, added.length);
            try {
                return new SecureString(newChars);
            } finally {
                Security.wipe(newChars);
                Security.wipe(chars);
                Security.wipe(plainData);
            }
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException("cannot decrypt", e);
        }
    }
    
    public SecureString delete(int count) {
        try {
            Cipher cipher = Security.getAESPadding();
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] plainData = cipher.doFinal(data);
            char[] chars = Charset.defaultCharset().decode(ByteBuffer.wrap(plainData)).array();
            
            int newSize = chars.length - count;
            if (newSize <= 0) {
                return null;
            }
            
            char[] newChars = new char[newSize];
            System.arraycopy(chars, 0, newChars, 0, newSize);
            try {
                return new SecureString(newChars);
            } finally {
                Security.wipe(newChars);
                Security.wipe(chars);
                Security.wipe(plainData);
            }
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException("cannot decrypt", e);
        }
    }
    
    public void accept(Consumer<char[]> consumer) {
        byte[] plainData;
        try {
            Cipher cipher = Security.getAESPadding();
            cipher.init(Cipher.DECRYPT_MODE, key);
            plainData = cipher.doFinal(data);
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException("cannot decrypt", e);
        }
        
        char[] copy = Charset.defaultCharset().decode(ByteBuffer.wrap(plainData)).array();
        try {
            consumer.accept(copy);
        } finally {
            Security.wipe(copy);
            Security.wipe(plainData);
        }
    }
    
    public <T> T apply(Function<char[], T> fn) {
        byte[] plainData;
        try {
            Cipher cipher = Security.getAESPadding();
            cipher.init(Cipher.DECRYPT_MODE, key);
            plainData = cipher.doFinal(data);
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException("cannot decrypt", e);
        }
        
        char[] copy = Charset.defaultCharset().decode(ByteBuffer.wrap(plainData)).array();
        
        try {
            return fn.apply(copy);
        } finally {
            Security.wipe(copy);
            Security.wipe(plainData);
        }
    }

    @Override
    public void close() {
        cleanable.clean();
    }
}
