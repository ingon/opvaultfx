package dev.ingon.opvault;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;

public class Security {
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
    
    static Cipher getAESPadding() {
        try {
            return Cipher.getInstance("AES");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new Error("cannot make AES", e);
        }
    }
    
    static MessageDigest getSHA256() {
        try {
            return MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException e) {
            throw new Error("cannot make SHA256 md", e);
        }
    }
    
    static byte[] encode(char[] src) {
        return encode(CharBuffer.wrap(src));
    }

    static byte[] encode(char[] src, int offset, int len) {
        return encode(CharBuffer.wrap(src, offset, len));
    }

    static byte[] encode(CharBuffer cb) {
        ByteBuffer bb = Charset.defaultCharset().encode(cb);
        byte[] bbarr = bb.array();
        if (bbarr.length == bb.remaining()) {
            return bbarr;
        }
        try {
            byte[] result = new byte[bb.remaining()];
            bb.get(result);
            return result;
        } finally {
            wipe(bbarr);
        }
    }

    static char[] decode(byte[] src) {
        ByteBuffer bb = ByteBuffer.wrap(src);
        CharBuffer cb = Charset.defaultCharset().decode(bb);
        char[] cbarr = cb.array();
        if (cbarr.length == cb.remaining()) {
            return cbarr;
        }
        try {
            char[] result = new char[cb.remaining()];
            cb.get(result);
            return result;
        } finally {
            wipe(cbarr);
        }
    }
    
    static byte[] decode(String data) {
        return Base64.getDecoder().decode(data);
    }
    
    public static void wipe(byte[] data) {
        for (int i = 0, n = data.length; i < n; i++) {
            data[i] = 0;
        }
    }

    public static void wipe(char[] data) {
        for (int i = 0, n = data.length; i < n; i++) {
            data[i] = '\0';
        }
    }
}
