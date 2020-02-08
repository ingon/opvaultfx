package org.abpass.otp;

import java.lang.ref.Cleaner;
import java.lang.ref.Cleaner.Cleanable;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.abpass.opvault.Security;

public abstract class OTPGenerator implements AutoCloseable {
    private static final Cleaner cleaner = Cleaner.create();
    
    private static final int[] DIGITS_POWER =
        {1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000, 1000000000};
    
    private final OTPAlgorithm algorithm;
    private final byte[] secret;
    private final int digits;
    private final Cleanable cleanable;
    
    public OTPGenerator(OTPAlgorithm algorithm, byte[] secret, int digits) {
        this.cleanable = cleaner.register(this, () -> Security.wipe(secret));
        
        this.algorithm = algorithm;
        this.secret = secret;
        this.digits = digits;
    }
    
    @Override
    public void close() throws Exception {
        cleanable.clean();
    }
    
    public abstract String generate();
    
    protected String generateFromState(long counter) {
        byte[] input = ByteBuffer.allocate(8).putLong(counter).array();
        
        Mac mac = getHmac();
        byte[] data = mac.doFinal(input);
        
        int offset = data[data.length - 1] & 0x0f;
        int hash = ByteBuffer.wrap(data, offset, 4).getInt();
        int truncatedHash = hash & 0x7FFFFFFF;
        int pin = truncatedHash % DIGITS_POWER[digits];
        
        var sb = new StringBuilder();
        sb.append(pin);
        while (sb.length() < digits) {
            sb.insert(0, "0");
        }
        return sb.toString();
    }

    private Mac getHmac() {
        try {
            var mac = Mac.getInstance(algorithm.macName);
            mac.init(new SecretKeySpec(secret, algorithm.keyName));
            return mac;
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new Error("cannot make " + algorithm, e);
        }
    }
}
