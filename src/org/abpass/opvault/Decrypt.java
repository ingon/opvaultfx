package org.abpass.opvault;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.abpass.opvault.Exceptions.InvalidOpdataException;

public class Decrypt {
    private static final byte[] HEADER = new byte[]{'o', 'p', 'd', 'a', 't', 'a', '0', '1'};
    private static final int HEADER_INDEX = 0;
    private static final int HEADER_SIZE = HEADER.length;
    private static final int PLAIN_TEXT_LENGTH_SIZE = 8;
    private static final int IV_SIZE = 16;
    private static final int MAC_SIZE = 32;
    private static final int MIN_LENGTH = HEADER_SIZE + PLAIN_TEXT_LENGTH_SIZE + IV_SIZE + 16 + MAC_SIZE;
    
    public static byte[] opdata(byte[] text, byte[] encKey, byte[] macKey) throws InvalidOpdataException {
        if (text.length < MIN_LENGTH) {
            throw new InvalidOpdataException("unexpected length");
        }
        
        var data = Arrays.copyOfRange(text, 0, text.length - MAC_SIZE);
        var macData = Arrays.copyOfRange(text, text.length - MAC_SIZE, text.length);
        
        Mac mac = getHmacSHA256();
        try { 
            mac.init(new SecretKeySpec(macKey, "SHA256"));
        } catch(InvalidKeyException exc) {
            throw new InvalidOpdataException(exc, "invalid key");
        }
        var calcMac = mac.doFinal(data);
        
        if (! Arrays.equals(macData, calcMac)) {
            throw new InvalidOpdataException("not equal");
        }
        
        if (! Arrays.equals(Arrays.copyOfRange(data, HEADER_INDEX, HEADER_SIZE), HEADER)) {
            throw new InvalidOpdataException("invalid header");
        }
        
        var bb = ByteBuffer.wrap(data, HEADER_SIZE, PLAIN_TEXT_LENGTH_SIZE);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        long plaintextLen = bb.getLong();
        
        var iv = Arrays.copyOfRange(data, HEADER_SIZE + PLAIN_TEXT_LENGTH_SIZE, HEADER_SIZE + PLAIN_TEXT_LENGTH_SIZE + IV_SIZE);
        var paddedData = Arrays.copyOfRange(data, HEADER_SIZE + PLAIN_TEXT_LENGTH_SIZE + IV_SIZE, data.length);
        if (paddedData.length < plaintextLen) {
            throw new InvalidOpdataException("invalid padded data");
        }
        
        SecretKeySpec spec = new SecretKeySpec(encKey, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        Cipher cipher = getAESNoPadding();
        
        try {
            cipher.init(Cipher.DECRYPT_MODE, spec, ivSpec);
            var decryptData = cipher.doFinal(paddedData);
            return Arrays.copyOfRange(decryptData, (int) (decryptData.length - plaintextLen), decryptData.length);
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
    
    static SecretKeyFactory getPBKDF2WithHmacSHA512() {
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
        } catch (NoSuchAlgorithmException e) {
            throw new Error("cannot make PBKDF2WithHmacSHA512", e);
        }
    }
    
    static Mac getHmacSHA256() {
        try {
            return Mac.getInstance("HmacSHA256");
        } catch (NoSuchAlgorithmException e) {
            throw new Error("cannot make HmacSHA256", e);
        }
    }
    
    static Cipher getAESNoPadding() {
        try {
            return Cipher.getInstance("AES/CBC/NoPadding");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new Error("cannot make AES/CBC/NoPadding", e);
        }
    }
    
    static byte[] decode(String data) {
        return Base64.getDecoder().decode(data);
    }
    
    static void wipe(byte[] data) {
        for (int i = 0, n = data.length; i < n; i++) {
            data[i] = 0;
        }
    }

    static void wipe(char[] data) {
        for (int i = 0, n = data.length; i < n; i++) {
            data[i] = '\0';
        }
    }
}
