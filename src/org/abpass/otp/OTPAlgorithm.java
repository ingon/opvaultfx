package org.abpass.otp;

public enum OTPAlgorithm {
    SHA1("HmacSHA1", "SHA1"),
    SHA256("HmacSHA256", "SHA256"),
    SHA512("HmacSHA512", "SHA512"),
    SHAMD5("HmacMD5", "MD5");
    
    public final String macName;
    public final String keyName;
    
    private OTPAlgorithm(String macName, String keyName) {
        this.macName = macName;
        this.keyName = keyName;
    }
}
