package org.abpass.opvault;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.crypto.spec.PBEKeySpec;

import org.abpass.json.JsonMapHandler;
import org.abpass.json.JsonParser;
import org.abpass.json.JsonTypedHandler;
import org.abpass.opvault.Exceptions.InvalidOpdataException;
import org.abpass.opvault.Exceptions.InvalidPasswordException;
import org.abpass.opvault.Exceptions.ProfileBandFormatException;
import org.abpass.opvault.Exceptions.ProfileBandReadException;
import org.abpass.opvault.Exceptions.ProfileException;
import org.abpass.opvault.Exceptions.ProfileFormatException;
import org.abpass.opvault.Exceptions.ProfileNotFileException;
import org.abpass.opvault.Exceptions.ProfileNotFoundException;
import org.abpass.opvault.Exceptions.ProfileReadException;
import org.json.simple.parser.ParseException;

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
        
        handler.stringProperty("lastUpdatedBy", (t, o) -> {});
        handler.stringProperty("profileName", (t, o) -> {});
        handler.instantProperty("createdAt", (t, o) -> {});
        handler.instantProperty("updatedAt", (t, o) -> {});
        
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
    
    Profile(Vault vault, Path path) throws ProfileException {
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
            
            JsonParser.parse(profileJson, newParser(this));
        } catch(IOException exc) {
            throw new ProfileReadException(profilePath, exc);
        } catch (ParseException exc) {
            throw new ProfileReadException(profilePath, exc);
        }
    }
    
    public String getUUID() {
        return uuid;
    }
    
    public void unlock(String password) throws InvalidPasswordException {
        var d = deriveKey(password);
        
        try {
            var master = d.decrypt(masterKey);
            Decrypt.wipe(master);
            
            this.derived = d;
        } catch(InvalidOpdataException exc) {
            throw new InvalidPasswordException(exc, path);
        }
    }
    
    private KeyMacPair deriveKey(String password) throws InvalidPasswordException {
        var keyFactory = Decrypt.getPBKDF2WithHmacSHA512();
        var keySpec = new PBEKeySpec(password.toCharArray(), salt, iterations, 64 * 8);
        
        try {
            var key = keyFactory.generateSecret(keySpec);
            
            byte[] keyData = key.getEncoded();
            try {
                return new KeyMacPair(Arrays.copyOfRange(keyData, 0, 32), Arrays.copyOfRange(keyData, 32, keyData.length));
            } finally {
                Decrypt.wipe(keyData);
            }
        } catch (InvalidKeySpecException e) {
            throw new InvalidPasswordException(e, path);
        } finally {
            keySpec.clearPassword();
        }
    }
    
    public void lock() {
        derived.close();
        derived = null;
    }
    
    public KeyMacPair overviewKeys() throws InvalidOpdataException, GeneralSecurityException {
        if (derived == null) {
            throw new IllegalStateException("profile locked");
        }
        
        var decryptedOverviewKey = derived.decrypt(overviewKey);
        
        var md = MessageDigest.getInstance("SHA-512");
        var keys = md.digest(decryptedOverviewKey);
        try {
            return new KeyMacPair(Arrays.copyOfRange(keys, 0, 32), Arrays.copyOfRange(keys, 32, keys.length));
        } finally {
            Decrypt.wipe(keys);
        }
    }

    KeyMacPair masterKeys() throws InvalidOpdataException, GeneralSecurityException {
        if (derived == null) {
            throw new IllegalStateException("profile locked");
        }
        
        var decryptedOverviewKey = derived.decrypt(masterKey);
        
        var md = MessageDigest.getInstance("SHA-512");
        var keys = md.digest(decryptedOverviewKey);
        try {
            return new KeyMacPair(Arrays.copyOfRange(keys, 0, 32), Arrays.copyOfRange(keys, 32, keys.length));
        } finally {
            Decrypt.wipe(keys);
        }
    }

    public List<Item> getItems() throws ProfileException {
        try (var ds = Files.newDirectoryStream(path, "band_[0123456789ABCDEF].js")) {
            var allItems = new ArrayList<Item>();
            for (var band : ds) {
                var items = getItemsInBand(band);
                System.out.println("items: " + items);
                allItems.addAll(items.values());
            }
            return allItems;
        } catch (IOException exc) {
            throw new ProfileReadException(path, exc);
        }
    }
    
    private Map<String, Item> getItemsInBand(Path band) throws ProfileException {
        try {
            String bandStr = Files.readString(band);
            if (! bandStr.startsWith(BAND_PREAMBLE) || ! bandStr.endsWith(BAND_EPILOGUE)) {
                throw new ProfileBandFormatException(path);
            }

            String bandJsonStr = bandStr.substring(BAND_PREAMBLE.length(), bandStr.length() - BAND_EPILOGUE.length());
            return JsonParser.parse(bandJsonStr, newBandParser(this));
        } catch (IOException exc) {
            throw new ProfileBandReadException(band, exc);
        } catch (ParseException exc) {
            throw new ProfileBandReadException(band, exc);
        }
    }
}
