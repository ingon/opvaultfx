package org.abpass.opvault;

import java.time.Instant;

import org.abpass.opvault.ItemException.ItemDetailKeyException;
import org.abpass.opvault.ItemException.ItemDetailParseException;
import org.abpass.opvault.ItemException.ItemOverviewKeyException;
import org.abpass.opvault.ItemException.ItemOverviewParseException;
import org.abpass.opvault.ProfileException.ProfileKeysException;
import org.abpass.opvault.ProfileException.ProfileLockedException;
import org.json.zero.ParseException;
import org.json.zero.hl.JsonParser;
import org.json.zero.hl.JsonTypedHandler;

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
    
    private byte[] hmac; // TODO verify item hmac!!!
    private byte[] o;
    private byte[] k;
    private byte[] d;
    
    Item(Profile profile) {
        this.profile = profile;
    }
    
    public String getUuid() {
        return uuid;
    }
    
    public Category getCategory() {
        return category;
    }
    
    public Long getFave() {
        return fave;
    }
    
    public String getFolder() {
        return folder;
    }
    
    public boolean isTrashed() {
        return trashed;
    }
    
    public Instant getCreated() {
        return created;
    }
    
    public Instant getUpdated() {
        return updated;
    }
    
    public Instant getTx() {
        return tx;
    }
    
    public ItemOverview getOverview() throws ProfileLockedException, ItemOverviewKeyException, ItemOverviewParseException {
        try (var keys = profile.overviewKeys()) {
            return getOverview(keys);
        } catch (ProfileKeysException e) {
            throw new ItemOverviewKeyException(e);
        }
    }
    
    public ItemOverview getOverview(OPData overviewKeys) throws ItemOverviewKeyException, ItemOverviewParseException {
        try {
            char[] overview = overviewKeys.decryptData(o);
            try {
                return JsonParser.parse(overview, ItemOverview.newParser());
            } catch (ParseException e) {
                throw new ItemOverviewParseException(e);
            } finally {
                Security.wipe(overview);
            }
        } catch (OPDataException e) {
            throw new ItemOverviewKeyException(e);
        }
    }
        
    public ItemDetail getDetail() throws ProfileLockedException, ItemDetailParseException, ItemDetailKeyException {
        try (var master = profile.masterKeys(); var item = master.decryptConcreteKeys(k)) {
            char[] detail = item.decryptData(d);
            try {
                return JsonParser.parse(detail, ItemDetail.newParser());
            } catch (ParseException e) {
                throw new ItemDetailParseException(e);
            } finally {
                Security.wipe(detail);
            }
        } catch (OPDataException e) {
            throw new ItemDetailKeyException(e);
        } catch (ProfileKeysException e) {
            throw new ItemDetailKeyException(e);
        }
    }
}
