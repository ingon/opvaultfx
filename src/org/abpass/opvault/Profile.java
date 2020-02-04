package org.abpass.opvault;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.abpass.opvault.ProfileException.InvalidPasswordException;
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
        handler.numberProperty("iterations", (t, o) -> t.iterations = o.intValue());
        handler.base64Property("salt", (t, o) -> t.salt = o);
        handler.base64Property("overviewKey", (t, o) -> t.overviewKey = o);
        handler.base64Property("masterKey", (t, o) -> t.masterKey = o);
        
        // TODO
        handler.stringProperty("lastUpdatedBy", (t, o) -> {});
        handler.stringProperty("profileName", (t, o) -> {});
        handler.instantProperty("createdAt", (t, o) -> {});
        handler.instantProperty("updatedAt", (t, o) -> {});
        handler.instantProperty("tx", (t, o) -> {});
        handler.stringProperty("passwordHint", (t, o) -> {});
        
        return handler;
    }
    
    static JsonMapHandler<Item> newBandParser(Profile profile) {
        return new JsonMapHandler<>(Item.newParser(profile));
    }

    public final Vault vault;
    public final Path path;
    
    private String uuid;
    
    private int iterations;
    private byte[] salt;
    private byte[] overviewKey;
    private byte[] masterKey;
    
    private KeyMacPair derived;
    
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
    
    public void unlock(SecureString password) throws InvalidPasswordException {
        var d = KeyMacPair.derive(password, salt, iterations);
        
        try (var master = d.decryptOpdataKeys(masterKey)) {
            this.derived = d;
        } catch(KeyMacPairException exc) {
            d.close();
            throw new InvalidPasswordException(exc, path);
        }
    }
    
    public void lock() {
        derived.close();
        derived = null;
    }
    
    public KeyMacPair overviewKeys() throws ProfileLockedException, ProfileKeysException {
        if (derived == null) {
            throw new ProfileLockedException(path);
        }
        
        try {
            return derived.decryptOpdataKeys(overviewKey);
        } catch (KeyMacPairException e) {
            throw new ProfileKeysException(path, e);
        }
    }

    KeyMacPair masterKeys() throws ProfileLockedException, ProfileKeysException {
        if (derived == null) {
            throw new ProfileLockedException(path);
        }
        
        try {
            return derived.decryptOpdataKeys(masterKey);
        } catch (KeyMacPairException e) {
            throw new ProfileKeysException(path, e);
        }
    }

    public List<Item> getItems() throws ProfileReadException, ProfileBandFormatException, ProfileBandReadException {
        try (var ds = Files.newDirectoryStream(path, "band_[0123456789ABCDEF].js")) {
            var allItems = new ArrayList<Item>();
            for (var band : ds) {
                var items = getItemsInBand(band);
                allItems.addAll(items.values());
            }
            return allItems;
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
}
