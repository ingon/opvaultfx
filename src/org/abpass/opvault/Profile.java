package org.abpass.opvault;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.abpass.opvault.ProfileException.InvalidPasswordException;
import org.abpass.opvault.ProfileException.ProfileAttachmentException;
import org.abpass.opvault.ProfileException.ProfileBandFormatException;
import org.abpass.opvault.ProfileException.ProfileBandReadException;
import org.abpass.opvault.ProfileException.ProfileFormatException;
import org.abpass.opvault.ProfileException.ProfileKeysException;
import org.abpass.opvault.ProfileException.ProfileLockedException;
import org.abpass.opvault.ProfileException.ProfileNotFileException;
import org.abpass.opvault.ProfileException.ProfileNotFoundException;
import org.abpass.opvault.ProfileException.ProfileReadException;
import org.json.zero.ParseException;
import org.json.zero.hl.JsonMapHandler;
import org.json.zero.hl.JsonParser;
import org.json.zero.hl.JsonTypedHandler;

public class Profile {
    public static final String PROFILE_PREAMBLE = "var profile=";
    public static final String PROFILE_EPILOGUE = ";";

    public static final String BAND_PREAMBLE = "ld(";
    public static final String BAND_EPILOGUE = ");";

    static JsonTypedHandler<Profile> newParser(Profile profile) {
        Json<Profile> handler = new Json<Profile>(() -> profile);
        handler.stringProperty("uuid", (t, o) -> t.uuid = o);
        
        handler.stringProperty("passwordHint", (t, o) -> t.passwordHint = o);
        handler.stringProperty("profileName", (t, o) -> t.profileName = o);
        handler.stringProperty("lastUpdatedBy", (t, o) -> t.lastUpdatedBy = o);
        handler.instantProperty("createdAt", (t, o) -> t.createdAt = o);
        handler.instantProperty("updatedAt", (t, o) -> t.updatedAt = o);
        handler.instantProperty("tx", (t, o) -> t.tx = o);
        
        handler.numberProperty("iterations", (t, o) -> t.iterations = o.intValue());
        handler.base64Property("salt", (t, o) -> t.salt = o);
        handler.base64Property("overviewKey", (t, o) -> t.overviewKey = o);
        handler.base64Property("masterKey", (t, o) -> t.masterKey = o);
        
        return handler;
    }
    
    static JsonMapHandler<Item> newBandParser(Profile profile) {
        return new JsonMapHandler<>(Item.newParser(profile));
    }

    public final Vault vault;
    public final Path path;
    
    private String uuid;
    
    private String passwordHint;
    private String profileName;
    private String lastUpdatedBy;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant tx;
    
    private int iterations;
    private byte[] salt;
    private byte[] overviewKey;
    private byte[] masterKey;
    
    private OPData derived;
    
    Profile(Vault vault, Path path) throws ProfileNotFoundException, ProfileNotFileException, ProfileFormatException, ProfileReadException {
        var profilePath = path.resolve("profile.js");
        if (! Files.exists(profilePath)) {
            throw new ProfileNotFoundException(profilePath);
        }
        if (! Files.isRegularFile(profilePath)) {
            throw new ProfileNotFileException(profilePath);
        }
        
        this.vault = vault;
        this.path = path;
        
        try {
            var profileData = Files.readString(profilePath);
            if (! profileData.startsWith(PROFILE_PREAMBLE) || ! profileData.endsWith(PROFILE_EPILOGUE)) {
                throw new ProfileFormatException(profilePath);
            }
            
            var profileJson = profileData.substring(PROFILE_PREAMBLE.length(), profileData.length() - PROFILE_EPILOGUE.length());
            JsonParser.parse(profileJson.toCharArray(), newParser(this));
        } catch(IOException exc) {
            throw new ProfileReadException(profilePath, exc);
        } catch (ParseException exc) {
            throw new ProfileReadException(profilePath, exc);
        }
    }
    
    public String getUUID() {
        return uuid;
    }
    
    public String getPasswordHint() {
        return passwordHint;
    }
    
    public String getProfileName() {
        return profileName;
    }
    
    public String getLastUpdatedBy() {
        return lastUpdatedBy;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public Instant getUpdatedAt() {
        return updatedAt;
    }
    
    public Instant getTx() {
        return tx;
    }
    
    public void unlock(SecureString password) throws InvalidPasswordException {
        var d = OPData.derive(password, salt, iterations);
        
        try (var master = d.decryptGeneralKeys(masterKey)) {
            this.derived = d;
        } catch(OPDataException exc) {
            d.close();
            throw new InvalidPasswordException(exc, path);
        }
    }
    
    public void lock() {
        derived.close();
        derived = null;
    }
    
    public OPData overviewKeys() throws ProfileLockedException, ProfileKeysException {
        if (derived == null) {
            throw new ProfileLockedException(path);
        }
        
        try {
            return derived.decryptGeneralKeys(overviewKey);
        } catch (OPDataException e) {
            throw new ProfileKeysException(path, e);
        }
    }

    OPData masterKeys() throws ProfileLockedException, ProfileKeysException {
        if (derived == null) {
            throw new ProfileLockedException(path);
        }
        
        try {
            return derived.decryptGeneralKeys(masterKey);
        } catch (OPDataException e) {
            throw new ProfileKeysException(path, e);
        }
    }

    public List<Item> getItems() throws ProfileReadException, ProfileBandFormatException, ProfileBandReadException, ProfileAttachmentException {
        try (var ds = Files.newDirectoryStream(path, "band_[0123456789ABCDEF].js")) {
            var allItems = new HashMap<String, Item>();
            for (var band : ds) {
                var items = getItemsInBand(band);
                allItems.putAll(items);
            }
            
            loadAttachments(allItems);
            
            return new ArrayList<Item>(allItems.values());
        } catch (IOException exc) {
            throw new ProfileReadException(path, exc);
        }
    }
    
    private Map<String, Item> getItemsInBand(Path band) throws ProfileBandFormatException, ProfileBandReadException {
        try {
            String bandStr = Files.readString(band);
            if (! bandStr.startsWith(BAND_PREAMBLE) || ! bandStr.endsWith(BAND_EPILOGUE)) {
                throw new ProfileBandFormatException(path);
            }

            String bandJsonStr = bandStr.substring(BAND_PREAMBLE.length(), bandStr.length() - BAND_EPILOGUE.length());
            return JsonParser.parse(bandJsonStr.toCharArray(), newBandParser(this));
        } catch (IOException exc) {
            throw new ProfileBandReadException(band, exc);
        } catch (ParseException exc) {
            throw new ProfileBandReadException(band, exc);
        }
    }
    
    private void loadAttachments(Map<String, Item> items) throws ProfileReadException, ProfileAttachmentException {
        try (var ds = Files.newDirectoryStream(path, "*.attachment")) {
            for (var attachmentPath : ds) {
                String name = attachmentPath.getFileName().toString();
                String itemUUID = name.substring(0, name.indexOf("_"));
                Item item = items.get(itemUUID);
                if (item == null) {
                    throw new ProfileAttachmentException(attachmentPath);
                }
                
                byte[] data = Files.readAllBytes(attachmentPath);
                ItemAttachment.loadAttachment(item, attachmentPath, data);
            }
        } catch (IOException | ItemAttachmentException exc) {
            throw new ProfileReadException(path, exc);
        }
    }
}
