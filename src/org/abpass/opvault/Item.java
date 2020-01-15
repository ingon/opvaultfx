package org.abpass.opvault;

import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.abpass.opvault.Exceptions.InvalidOpdataException;

public class Item {
    public enum Category {
        Login("001"), 
        CreditCard("002"), 
        SecureNote("003"), 
        Identity("004"), 
        Password("005"), 
        Tombstone("099"),
        SoftwareLicense("100"), 
        BankAccount("101"), 
        Database("102"), 
        DriverLicense("103"), 
        OutdoorLicense("104"),
        Membership("105"), 
        Passport("106"), 
        Rewards("107"), 
        SSN("108"), 
        Router("109"), 
        Server("110"), 
        Email("111"),
        ;

        private final String raw;

        private Category(String raw) {
            this.raw = raw;
        }

        public static Category of(String s) {
            for (var e : Category.values()) {
                if (e.raw.equals(s)) {
                    return e;
                }
            }
            throw new IllegalArgumentException("unknown category: " + s);
        }
    }
    
    public final Profile profile;
    public final Map<String, Object> data;
    
    public Item(Profile profile, Map<String, Object> data) {
        this.profile = profile;
        this.data = data;
    }
    
    public String getUUID() {
        return (String) data.get("uuid");
    }
    
    public Category getCategory() {
        var s = (String) data.get("category");
        return Category.of(s);
    }
    
    public ItemOverview getOverview() throws InvalidOpdataException, GeneralSecurityException {
        try (var keys = profile.overviewKeys()) {
            return getOverview(keys);
        }
    }
    
    public ItemOverview getOverview(KeyMacPair overviewKeys) throws InvalidOpdataException, GeneralSecurityException {
        var overviewData = getAsBytes("o");
        if (overviewData.length == 0) {
            return new ItemOverview(Collections.emptyMap());
        }
        
        byte[] decOverviewData = overviewKeys.decrypt(overviewData);
        Map<String, Object> jsonData = Json.parse(decOverviewData);
        return new ItemOverview(jsonData);
    }
    
    public Instant getCreated() {
        var n = (Number) data.get("created");
        return Instant.ofEpochSecond(n.longValue());
    }

    public Instant getUpdated() {
        var n = (Number) data.get("updated");
        return Instant.ofEpochSecond(n.longValue());
    }

    public Instant getTx() {
        var n = (Number) data.get("tx");
        return Instant.ofEpochSecond(n.longValue());
    }

    public Long getFave() {
        var n = (Number) data.get("fave");
        if (n == null) {
            return null;
        }
        return n.longValue();
    }
    
    public String getFolder() {
        return (String) data.get("folder");
    }

    public boolean isTrashed() {
        return (Boolean) data.get("trashed");
    }
    
    public ItemDetail getDetail() throws InvalidOpdataException, GeneralSecurityException {
        var detailsData = itemKeys().decrypt(getAsBytes("d"));
        Map<String, Object> jsonData = Json.parse(detailsData);
        return new ItemDetail(jsonData);
    }
    
    private KeyMacPair itemKeys() throws InvalidOpdataException, GeneralSecurityException {
        byte[] key = getAsBytes("k");
        if (key.length == 0) {
            throw new RuntimeException("no key");
        }
        
        var master = profile.masterKeys();
        
        var data = Arrays.copyOfRange(key, 0, key.length - 32);
        var mac = Arrays.copyOfRange(key, key.length - 32, key.length);
        
        var mm = Mac.getInstance("HmacSHA256");
        mm.init(new SecretKeySpec(master.mac, "SHA256"));
        var calcMac = mm.doFinal(data);
        if (! Arrays.equals(mac, calcMac)) {
            throw new RuntimeException("invalid keys");
        }
        
        SecretKeySpec spec = new SecretKeySpec(master.key, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(Arrays.copyOfRange(data, 0, 16));
        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, spec, ivSpec);
        var keys = cipher.doFinal(Arrays.copyOfRange(data, 16, data.length));
        
        return new KeyMacPair(Arrays.copyOfRange(keys, keys.length - 64, keys.length - 32), 
            Arrays.copyOfRange(keys, keys.length - 32, keys.length));
    }

    private byte[] getAsBytes(String key) {
        var val = (String) data.get(key);
        return Base64.getDecoder().decode(val);
    }
    
    @Override
    public String toString() {
        return String.format("item [ uuid=%s, category=%s, created=%s, updated=%s, tx=%s, fave=%s, folder=%s ]", 
            getUUID(), getCategory(), getCreated(), getUpdated(), getTx(), getFave(), getFolder());
    }
}
