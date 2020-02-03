package org.abpass.opvault;

import java.lang.ref.Cleaner;
import java.lang.ref.Cleaner.Cleanable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.abpass.opvault.Exceptions.InvalidOpdataException;

// TODO: should we encrypt these like SecureString
public final class KeyMacPair implements AutoCloseable {
    private static final Cleaner cleaner = Cleaner.create();
    
    private final Cleanable cleanable;
    private final byte[] key;
    private final byte[] mac;
    
    private KeyMacPair(byte[] combined, int keyFrom, int keyTo, int macFrom, int macTo) {
        byte[] key = Arrays.copyOfRange(combined, keyFrom, keyTo);
        byte[] mac = Arrays.copyOfRange(combined, macFrom, macTo);
        
        this.cleanable = cleaner.register(this, () -> {
            Security.wipe(key);
            Security.wipe(mac);
        });
        this.key = key;
        this.mac = mac;
    }
    
    public static KeyMacPair derive(SecureString password, byte[] salt, int iterations) {
        var keyFactory = Security.getPBKDF2WithHmacSHA512();
        PBEKeySpec keySpec = password.apply((chs) -> new PBEKeySpec(chs, salt, iterations, 64 * 8));
        try {
            SecretKey key = keyFactory.generateSecret(keySpec);
            byte[] keyData = key.getEncoded();
            try {
                return new KeyMacPair(keyData, 0, 32, 32, keyData.length);
            } finally {
                Security.wipe(keyData);
            }
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException("unexpected exc", e);
        } finally {
            keySpec.clearPassword();
        }
    }
    
    public KeyMacPair decryptKeys(byte[] encData) throws InvalidOpdataException, GeneralSecurityException {
        verifyMac(encData, mac);
        
        // extract the keys
        Cipher cipher = Security.getAESNoPadding();
        IvParameterSpec ivSpec = new IvParameterSpec(encData, 0, 16);
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), ivSpec);
        byte[] keys = cipher.doFinal(encData, 16, encData.length - 32 - 16);
        try {
            return new KeyMacPair(keys, keys.length - 64, keys.length - 32, keys.length - 32, keys.length);
        } finally {
            Security.wipe(keys);
        }
    }
    
    public KeyMacPair decryptOpdataKeys(byte[] encData) throws InvalidOpdataException {
        byte[] keyData = opdata(encData);
        try {
            var md = Security.getSHA256();
            byte[] keys = md.digest(keyData); // digest actually resets
            try {
                return new KeyMacPair(keys, 0, 32, 32, keys.length);
            } finally {
                Security.wipe(keys);
            }
        } finally {
            Security.wipe(keyData);
        }
    }
    
    public char[] decryptOpdata(byte[] encData) throws InvalidOpdataException {
        byte[] decData = opdata(encData);
        try {
            // the following sequence is zero copy
            var bytes = ByteBuffer.wrap(decData);
            var chars = Charset.defaultCharset().decode(bytes);
            return chars.array();
        } finally {
            Security.wipe(decData);
        }
    }

    private static final byte[] HEADER = new byte[]{'o', 'p', 'd', 'a', 't', 'a', '0', '1'};
    private static final int HEADER_INDEX = 0;
    private static final int HEADER_SIZE = HEADER.length;
    private static final int PLAIN_TEXT_LENGTH_SIZE = 8;
    private static final int IV_SIZE = 16;
    private static final int MAC_SIZE = 32;
    private static final int MIN_LENGTH = HEADER_SIZE + PLAIN_TEXT_LENGTH_SIZE + IV_SIZE + 16 + MAC_SIZE;
    
    public static void verifyMac(byte[] data, byte[] macKey) throws InvalidOpdataException {
        Mac mac = Security.getHmacSHA256();
        try {
            mac.init(new SecretKeySpec(macKey, "SHA256"));
            var dataMac = mac.doFinal(Arrays.copyOfRange(data, 0, data.length - MAC_SIZE));
            
            if (! Arrays.equals(data, data.length - MAC_SIZE, data.length, dataMac, 0, dataMac.length)) {
                throw new InvalidOpdataException("mac check failed");
            }
        } catch (InvalidKeyException exc) {
            throw new InvalidOpdataException("invalid keys");
        } finally {
            mac.reset();
        }
    }

    private byte[] opdata(byte[] text) throws InvalidOpdataException {
        if (text.length < MIN_LENGTH) {
            throw new InvalidOpdataException("unexpected length");
        }

        verifyMac(text, mac);
        
        if (! Arrays.equals(text, HEADER_INDEX, HEADER_SIZE, HEADER, 0, HEADER_SIZE)) {
            throw new InvalidOpdataException("invalid header");
        }
        
        var bb = ByteBuffer.wrap(text, HEADER_SIZE, PLAIN_TEXT_LENGTH_SIZE);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        long plaintextLen = bb.getLong();
        
        int paddedLen = text.length - HEADER_SIZE - PLAIN_TEXT_LENGTH_SIZE - IV_SIZE - MAC_SIZE;
        if (paddedLen < plaintextLen) {
            throw new InvalidOpdataException("invalid padded data");
        }
        
        Cipher cipher = Security.getAESNoPadding();
        IvParameterSpec ivSpec = new IvParameterSpec(text, HEADER_SIZE + PLAIN_TEXT_LENGTH_SIZE, IV_SIZE);
        try {
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), ivSpec);
            byte[] decryptData = cipher.doFinal(text, HEADER_SIZE + PLAIN_TEXT_LENGTH_SIZE + IV_SIZE, paddedLen);
            try {
                return Arrays.copyOfRange(decryptData, (int) (decryptData.length - plaintextLen), decryptData.length);
            } finally {
                Security.wipe(decryptData);
            }
        } catch (InvalidKeyException e) {
            throw new InvalidOpdataException(e, "invalid key");
        } catch (InvalidAlgorithmParameterException e) {
            throw new InvalidOpdataException(e, "invalid algorithm");
        } catch (IllegalBlockSizeException e) {
            throw new InvalidOpdataException(e, "illegal block size");
        } catch (BadPaddingException e) {
            throw new InvalidOpdataException(e, "bad padding");
        }
    }

    @Override
    public void close() {
        cleanable.clean();
    }
}
