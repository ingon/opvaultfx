package org.abpass.opvault;

import java.lang.ref.Cleaner;
import java.lang.ref.Cleaner.Cleanable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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

// TODO: should we encrypt these like SecureString
// TODO: maybe we should just mark the offset/len of key/mac
public final class KeyMacPair implements AutoCloseable {
    private static final Cleaner cleaner = Cleaner.create();
    
    private final Cleanable cleanable;
    private final byte[] key;
    private final byte[] mac;
    
    private KeyMacPair(byte[] source, int keyFrom, int keyTo, int macFrom, int macTo) {
        byte[] key = Arrays.copyOfRange(source, keyFrom, keyTo);
        byte[] mac = Arrays.copyOfRange(source, macFrom, macTo);
        
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
            throw new Error("unexpected exc", e);
        } finally {
            keySpec.clearPassword();
        }
    }
    
    public KeyMacPair decryptKeys(byte[] encData) throws KeyMacPairException {
        verifyMac(encData, mac);
        
        // extract the keys
        Cipher cipher = Security.getAESNoPadding();
        IvParameterSpec ivSpec = new IvParameterSpec(encData, 0, 16);
        try {
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), ivSpec);
            byte[] keys = cipher.doFinal(encData, 16, encData.length - 32 - 16);
            try {
                return new KeyMacPair(keys, keys.length - 64, keys.length - 32, keys.length - 32, keys.length);
            } finally {
                Security.wipe(keys);
            }
        } catch (InvalidKeyException e) {
            throw new KeyMacPairException(e, "invalid key");
        } catch (IllegalBlockSizeException e) {
            throw new KeyMacPairException(e, "illegal block size");
        } catch (BadPaddingException e) {
            throw new KeyMacPairException(e, "bad padding");
        } catch (InvalidAlgorithmParameterException exc) {
            throw new Error("invalid algorithm param", exc);
        }
    }
    
    public KeyMacPair decryptOpdataKeys(byte[] encData) throws KeyMacPairException {
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
    
    public char[] decryptOpdata(byte[] encData) throws KeyMacPairException {
        byte[] decData = opdata(encData);
        try {
            return Security.decode(decData);
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
    
    public static void verifyMac(byte[] data, byte[] macKey) throws KeyMacPairException {
        Mac mac = Security.getHmacSHA256();
        try {
            mac.init(new SecretKeySpec(macKey, "SHA256"));
            var dataMac = mac.doFinal(Arrays.copyOfRange(data, 0, data.length - MAC_SIZE));
            
            if (! Arrays.equals(data, data.length - MAC_SIZE, data.length, dataMac, 0, dataMac.length)) {
                throw new KeyMacPairException("mac check failed");
            }
        } catch (InvalidKeyException exc) {
            throw new KeyMacPairException("invalid keys");
        } finally {
            mac.reset();
        }
    }

    private byte[] opdata(byte[] text) throws KeyMacPairException {
        if (text.length < MIN_LENGTH) {
            throw new KeyMacPairException("unexpected length");
        }

        verifyMac(text, mac);
        
        if (! Arrays.equals(text, HEADER_INDEX, HEADER_SIZE, HEADER, 0, HEADER_SIZE)) {
            throw new KeyMacPairException("invalid header");
        }
        
        var bb = ByteBuffer.wrap(text, HEADER_SIZE, PLAIN_TEXT_LENGTH_SIZE);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        long plaintextLen = bb.getLong();
        
        int paddedLen = text.length - HEADER_SIZE - PLAIN_TEXT_LENGTH_SIZE - IV_SIZE - MAC_SIZE;
        if (paddedLen < plaintextLen) {
            throw new KeyMacPairException("invalid padded data");
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
            throw new KeyMacPairException(e, "invalid key");
        } catch (IllegalBlockSizeException e) {
            throw new KeyMacPairException(e, "illegal block size");
        } catch (BadPaddingException e) {
            throw new KeyMacPairException(e, "bad padding");
        } catch (InvalidAlgorithmParameterException e) {
            throw new Error("invalid algorithm param", e);
        }
    }

    @Override
    public void close() {
        cleanable.clean();
    }
}
