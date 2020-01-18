package org.abpass.opvault;

import org.abpass.opvault.Exceptions.InvalidOpdataException;

public class KeyMacPair implements AutoCloseable {
    public final byte[] key;
    public final byte[] mac;
    
    public KeyMacPair(byte[] key, byte[] mac) {
        this.key = key;
        this.mac = mac;
    }
    
    public byte[] decrypt(byte[] src) throws InvalidOpdataException {
        return Decrypt.opdata(src, key, mac);
    }

    @Override
    public void close() {
        Decrypt.wipe(key);
        Decrypt.wipe(mac);
    }
}
