package org.abpass.otp;

import java.net.URI;

public class TOTPGenerator extends OTPGenerator {
    private final int periodSec;
    
    public static TOTPGenerator fromURI(URI uri) {
        if (!"otpauth".equals(uri.getScheme())) {
            throw new IllegalArgumentException("invalid scheme");
        }
        if (!"totp".equals(uri.getHost())) {
            throw new IllegalArgumentException("only totp type is supported");
        }

        OTPAlgorithm algo = OTPAlgorithm.SHA1;
        byte[] secret = null;
        int digits = 6;
        int period = 30;
        for (var item : uri.getQuery().split("&")) {
            String[] parts = item.split("=");
            if ("secret".equals(parts[0])) {
                secret = Base32.decode(parts[1]);
            } else if ("algorithm".equals(parts[0])) {
                algo = OTPAlgorithm.valueOf(parts[1]);
            } else if ("digits".equals(parts[0])) {
                digits = Integer.parseInt(parts[1]);
            } else if ("period".equals(parts[0])) {
                period = Integer.parseInt(parts[1]);
            }
        }
        
        if (secret == null) {
            throw new IllegalArgumentException("secret not set");
        }
        
        return new TOTPGenerator(algo, secret, digits, period);
    }

    public TOTPGenerator(OTPAlgorithm algorithm, byte[] secret, int digits) {
        this(algorithm, secret, digits, 30);
    }

    public TOTPGenerator(OTPAlgorithm algorithm, byte[] secret, int digits, int periodSec) {
        super(algorithm, secret, digits);
        this.periodSec = periodSec;
    }
    
    @Override
    public String generate() {
        long seconds = System.currentTimeMillis() / 1000;
        long counter = seconds / periodSec;
        return super.generateFromState(counter);
    }
}
