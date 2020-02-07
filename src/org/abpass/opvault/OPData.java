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
public final class OPData implements AutoCloseable {
    private static final Cleaner cleaner = Cleaner.create();
    
    private final Cleanable cleanable;
    private final byte[] key;
    private final byte[] mac;
    
    private OPData(byte[] input, int keyFrom, int keyTo, int macFrom, int macTo) {
        byte[] key = Arrays.copyOfRange(input, keyFrom, keyTo);
        byte[] mac = Arrays.copyOfRange(input, macFrom, macTo);
        
        this.cleanable = cleaner.register(this, () -> {
            Security.wipe(key);
            Security.wipe(mac);
        });
        this.key = key;
        this.mac = mac;
    }
    
    public static OPData derive(SecureString password, byte[] salt, int iterations) {
        var keyFactory = Security.getPBKDF2WithHmacSHA512();
        PBEKeySpec keySpec = password.apply((chs) -> new PBEKeySpec(chs, salt, iterations, 64 * 8));
        try {
            SecretKey key = keyFactory.generateSecret(keySpec);
            byte[] keyData = key.getEncoded();
            try {
                return new OPData(keyData, 0, 32, 32, keyData.length);
            } finally {
                Security.wipe(keyData);
            }
        } catch (InvalidKeySpecException e) {
            throw new Error("unexpected exc", e);
        } finally {
            keySpec.clearPassword();
        }
    }
    
    public OPData decryptGeneralKeys(byte[] input) throws OPDataException {
        byte[] keyData = opdata(input);
        try {
            var md = Security.getSHA256();
            byte[] keys = md.digest(keyData); // digest actually resets
            try {
                return new OPData(keys, 0, 32, 32, keys.length);
            } finally {
                Security.wipe(keys);
            }
        } finally {
            Security.wipe(keyData);
        }
    }
    
    public OPData decryptConcreteKeys(byte[] input) throws OPDataException {
        verifyMac(input);
        
        // extract the keys
        Cipher cipher = Security.getAESNoPadding();
        IvParameterSpec ivSpec = new IvParameterSpec(input, 0, 16);
        try {
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), ivSpec);
            byte[] keys = cipher.doFinal(input, 16, input.length - 32 - 16);
            try {
                return new OPData(keys, keys.length - 64, keys.length - 32, keys.length - 32, keys.length);
            } finally {
                Security.wipe(keys);
            }
        } catch (InvalidKeyException e) {
            throw new OPDataException(e, "invalid key");
        } catch (IllegalBlockSizeException e) {
            throw new OPDataException(e, "illegal block size");
        } catch (BadPaddingException e) {
            throw new OPDataException(e, "bad padding");
        } catch (InvalidAlgorithmParameterException exc) {
            throw new Error("invalid algorithm param", exc);
        }
    }
    
    public char[] decryptData(byte[] input) throws OPDataException {
        byte[] decData = opdata(input);
        try {
            return Security.decode(decData);
        } finally {
            Security.wipe(decData);
        }
    }

    public byte[] decryptRawData(byte[] input) throws OPDataException {
        return opdata(input);
    }

    private static final byte[] HEADER = new byte[]{'o', 'p', 'd', 'a', 't', 'a', '0', '1'};
    private static final int HEADER_INDEX = 0;
    private static final int HEADER_SIZE = HEADER.length;
    private static final int PLAIN_TEXT_LENGTH_SIZE = 8;
    private static final int IV_SIZE = 16;
    private static final int MAC_SIZE = 32;
    private static final int MIN_LENGTH = HEADER_SIZE + PLAIN_TEXT_LENGTH_SIZE + IV_SIZE + 16 + MAC_SIZE;
    
    private void verifyMac(byte[] input) throws OPDataException {
        Mac mac = Security.getHmacSHA256();
        try {
            mac.init(new SecretKeySpec(this.mac, "SHA256"));
            var dataMac = mac.doFinal(Arrays.copyOfRange(input, 0, input.length - MAC_SIZE));
            
            if (! Arrays.equals(input, input.length - MAC_SIZE, input.length, dataMac, 0, dataMac.length)) {
                throw new OPDataException("mac check failed");
            }
        } catch (InvalidKeyException exc) {
            throw new OPDataException("invalid keys");
        } finally {
            mac.reset();
        }
    }

    private byte[] opdata(byte[] input) throws OPDataException {
        if (input.length < MIN_LENGTH) {
            throw new OPDataException("unexpected length");
        }

        verifyMac(input);
        
        if (! Arrays.equals(input, HEADER_INDEX, HEADER_SIZE, HEADER, 0, HEADER_SIZE)) {
            throw new OPDataException("invalid header");
        }
        
        var bb = ByteBuffer.wrap(input, HEADER_SIZE, PLAIN_TEXT_LENGTH_SIZE);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        long plaintextLen = bb.getLong();
        
        int paddedLen = input.length - HEADER_SIZE - PLAIN_TEXT_LENGTH_SIZE - IV_SIZE - MAC_SIZE;
        if (paddedLen < plaintextLen) {
            throw new OPDataException("invalid padded data");
        }
        
        Cipher cipher = Security.getAESNoPadding();
        IvParameterSpec ivSpec = new IvParameterSpec(input, HEADER_SIZE + PLAIN_TEXT_LENGTH_SIZE, IV_SIZE);
        try {
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), ivSpec);
            byte[] decryptData = cipher.doFinal(input, HEADER_SIZE + PLAIN_TEXT_LENGTH_SIZE + IV_SIZE, paddedLen);
            try {
                return Arrays.copyOfRange(decryptData, (int) (decryptData.length - plaintextLen), decryptData.length);
            } finally {
                Security.wipe(decryptData);
            }
        } catch (InvalidKeyException e) {
            throw new OPDataException(e, "invalid key");
        } catch (IllegalBlockSizeException e) {
            throw new OPDataException(e, "illegal block size");
        } catch (BadPaddingException e) {
            throw new OPDataException(e, "bad padding");
        } catch (InvalidAlgorithmParameterException e) {
            throw new Error("invalid algorithm param", e);
        }
    }

    @Override
    public void close() {
        cleanable.clean();
    }
}
