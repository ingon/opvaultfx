package org.abpass.opvault;

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
    
    static MessageDigest getSHA256() {
        try {
            return MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException e) {
            throw new Error("cannot make SHA256 md", e);
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
