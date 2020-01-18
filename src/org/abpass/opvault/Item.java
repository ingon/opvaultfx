package org.abpass.opvault;

import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.abpass.json.JsonParser;
import org.abpass.json.JsonTypedHandler;
import org.abpass.opvault.Exceptions.InvalidOpdataException;
import org.json.simple.parser.ParseException;

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
    
    static JsonTypedHandler<Item> newParser(Profile profile) {
        Json<Item> handler = new Json<Item>(() -> new Item(profile));
        handler.stringProperty("uuid", (t, o) -> t.uuid = o);
        handler.stringProperty("category", (t, o) -> t.category = Category.of(o));
        
        handler.numberProperty("fave", (t, o) -> t.fave = o.longValue());
        handler.stringProperty("folder", (t, o) -> t.folder = o);
        handler.booleanProperty("trashed", (t, o) -> t.trashed = o);
        
        handler.instantProperty("created", (t, o) -> t.created = o);
        handler.instantProperty("updated", (t, o) -> t.updated = o);
        handler.instantProperty("tx", (t, o) -> t.tx = o);
        
        handler.base64Property("hmac", (t, o) -> t.hmac = o);
        handler.base64Property("o", (t, o) -> t.o = o);
        handler.base64Property("k", (t, o) -> t.k = o);
        handler.base64Property("d", (t, o) -> t.d = o);

        return handler;
    }
    
    private final Profile profile;
    
    private String uuid;
    private Category category;
    
    private Long fave;
    private String folder;
    private boolean trashed;
    
    private Instant created;
    private Instant updated;
    private Instant tx;
    
    private byte[] hmac;
    private byte[] o;
    private byte[] k;
    private byte[] d;
    
    Item(Profile profile) {
        this.profile = profile;
    }
    
    public Category getCategory() {
        return category;
    }
    
    public ItemOverview getOverview() throws GeneralSecurityException, InvalidOpdataException, ParseException {
        try (var keys = profile.overviewKeys()) {
            return getOverview(keys);
        }
    }
    
    public ItemOverview getOverview(KeyMacPair overviewKeys) throws InvalidOpdataException, GeneralSecurityException, ParseException {
        byte[] overview = overviewKeys.decrypt(o);
        try {
            return JsonParser.parse(overview, ItemOverview.newParser());
        } finally {
            Decrypt.wipe(overview);
        }
    }
        
    public ItemDetail getDetail() throws InvalidOpdataException, GeneralSecurityException, ParseException {
        try (var keys = itemKeys()) {
            byte[] detail = keys.decrypt(d);
            try {
                return JsonParser.parse(detail, ItemDetail.newParser());
            } finally {
                Decrypt.wipe(detail);
            }
        }
    }
    
    private KeyMacPair itemKeys() throws InvalidOpdataException, GeneralSecurityException {
        var master = profile.masterKeys();
        
        var data = Arrays.copyOfRange(k, 0, k.length - 32);
        var mac = Arrays.copyOfRange(k, k.length - 32, k.length);
        
        // check data against its mac
        var mm = Mac.getInstance("HmacSHA256");
        mm.init(new SecretKeySpec(master.mac, "SHA256"));
        var calcMac = mm.doFinal(data);
        if (! Arrays.equals(mac, calcMac)) {
            throw new RuntimeException("invalid keys");
        }
        
        // extract the keys
        SecretKeySpec spec = new SecretKeySpec(master.key, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(Arrays.copyOfRange(data, 0, 16));
        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, spec, ivSpec);
        var keys = cipher.doFinal(Arrays.copyOfRange(data, 16, data.length));
        
        return new KeyMacPair(Arrays.copyOfRange(keys, keys.length - 64, keys.length - 32), 
            Arrays.copyOfRange(keys, keys.length - 32, keys.length));
    }
}
