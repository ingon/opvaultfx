package org.abpass.opvault;

import java.lang.ref.Cleaner;
import java.lang.ref.Cleaner.Cleanable;
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
    
    public SecureString(char[] source) {
        SecretKey key = generateKey();
        
        byte[] data;
        byte[] sourceData = Security.encode(source);
        try {
            Cipher cipher = Security.getAESPadding();
            cipher.init(Cipher.ENCRYPT_MODE, key);
            data = cipher.doFinal(sourceData);
        } catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            throw new RuntimeException("cannot encrypt", e);
        } finally {
            Security.wipe(sourceData);
        }
        
        this.cleanable = cleaner.register(this, () -> Security.wipe(data));
        
        this.key = key;
        this.data = data;
    }
    
    public SecureString(char[] source, int start, int len) {
        SecretKey key = generateKey();

        byte[] data;
        byte[] sourceData = Security.encode(source, start, len);
        try {
            Cipher cipher = Security.getAESPadding();
            cipher.init(Cipher.ENCRYPT_MODE, key);
            data = cipher.doFinal(sourceData);
        } catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            throw new RuntimeException("cannot encrypt", e);
        } finally {
            Security.wipe(sourceData);
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
            
            char[] chars = Security.decode(plainData);
            Security.wipe(plainData);
            
            char[] newChars = new char[chars.length + added.length];
            System.arraycopy(chars, 0, newChars, 0, chars.length);
            Security.wipe(chars);
            
            System.arraycopy(added, 0, newChars, chars.length, added.length);
            try {
                return new SecureString(newChars);
            } finally {
                Security.wipe(newChars);
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
            
            char[] chars = Security.decode(plainData);
            Security.wipe(plainData);
            
            int newSize = chars.length - count;
            if (newSize <= 0) {
                return null;
            }
            
            char[] newChars = new char[newSize];
            System.arraycopy(chars, 0, newChars, 0, newSize);
            Security.wipe(chars);
            
            try {
                return new SecureString(newChars);
            } finally {
                Security.wipe(newChars);
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
        
        char[] copy = Security.decode(plainData);
        Security.wipe(plainData);
        
        try {
            consumer.accept(copy);
        } finally {
            Security.wipe(copy);
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
        
        char[] copy = Security.decode(plainData);
        Security.wipe(plainData);
        
        try {
            return fn.apply(copy);
        } finally {
            Security.wipe(copy);
        }
    }

    @Override
    public void close() {
        cleanable.clean();
    }
}
