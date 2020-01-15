package org.abpass.opvault;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.abpass.opvault.Exceptions.InvalidOpdataException;
import org.abpass.opvault.Exceptions.InvalidPasswordException;
import org.abpass.opvault.Exceptions.ProfileCannotReadException;
import org.abpass.opvault.Exceptions.ProfileException;
import org.abpass.opvault.Exceptions.ProfileMissingPreambleException;
import org.abpass.opvault.Exceptions.ProfileNotFileException;
import org.abpass.opvault.Exceptions.ProfileNotFoundException;

public class Profile {
    public static final String preamble = "var profile=";
    
    public final Vault vault;
    public final Path path;
    
    public final Map<String, Object> data;
    private KeyMacPair derived;
    
    public Profile(Vault vault, Path path) throws ProfileException {
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
            if (! profileData.startsWith(preamble)) {
                throw new ProfileMissingPreambleException(profilePath);
            }
            
            var profileJson = profileData.substring(preamble.length(), profileData.length() - 1);
            this.data = Json.parse(profileJson);
        } catch(IOException exc) {
            throw new ProfileCannotReadException(profilePath, exc);
        }
    }
    
    public void unlock(String password) throws InvalidPasswordException, GeneralSecurityException {
        var keySpec = new PBEKeySpec(password.toCharArray(), salt(), iterations(), 64 * 8);
        var keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
        var key = keyFactory.generateSecret(keySpec);

        byte[] keyData = key.getEncoded();
        
        KeyMacPair p = new KeyMacPair(Arrays.copyOfRange(keyData, 0, 32), 
            Arrays.copyOfRange(keyData, 32, keyData.length));
        
        try {
            var master = p.decrypt(getAsBytes("masterKey"));
            Decrypt.wipe(master);
            
            derived = p;
        } catch(InvalidOpdataException exc) {
            throw new InvalidPasswordException(exc, path);
        }
    }
    
    public void lock() {
        derived.close();
        derived = null;
    }
    
    public KeyMacPair overviewKeys() throws InvalidOpdataException, GeneralSecurityException {
        if (derived == null) {
            throw new RuntimeException("locked");
        }
        
        var decryptedOverviewKey = derived.decrypt(getAsBytes("overviewKey"));
        
        var md = MessageDigest.getInstance("SHA-512");
        var keys = md.digest(decryptedOverviewKey);
        
        return new KeyMacPair(Arrays.copyOfRange(keys, 0, 32), Arrays.copyOfRange(keys, 32, keys.length));
    }

    KeyMacPair masterKeys() throws InvalidOpdataException, GeneralSecurityException {
        if (derived == null) {
            throw new RuntimeException("locked");
        }
        
        var decryptedOverviewKey = derived.decrypt(getAsBytes("masterKey"));
        
        var md = MessageDigest.getInstance("SHA-512");
        var keys = md.digest(decryptedOverviewKey);
        
        return new KeyMacPair(Arrays.copyOfRange(keys, 0, 32), Arrays.copyOfRange(keys, 32, keys.length));
    }

    public List<Item> getItems() throws IOException {
        try (var ds = Files.newDirectoryStream(path, "band_[0123456789ABCDEF].js")) {
            return StreamSupport.stream(ds.spliterator(), true)
                    .flatMap(this::getItemsInBand)
                    .collect(Collectors.toList());
        }
    }
    
    private Stream<Item> getItemsInBand(Path band) {
        try {
            String bandStr = Files.readString(band);
            if (!(bandStr.startsWith("ld(") && bandStr.endsWith(");"))) {
                // TODO throw
            }

            String bandJsonStr = bandStr.substring("ld(".length(), bandStr.length() - ");".length());
            var data = (Map<String, Object>) Json.parse(bandJsonStr);
            return data.values().stream().map((e) -> {
                return new Item(Profile.this, (Map<String, Object>) e);
            });
        } catch (IOException exc) {
            // TODO fix
            throw new RuntimeException(exc);
        }
    }
    
    byte[] salt() {
        return getAsBytes("salt");
    }
    
    int iterations() {
        return ((Number) data.get("iterations")).intValue();
    }
    
    private byte[] getAsBytes(String key) {
        var val = (String) data.get(key);
        return Base64.getDecoder().decode(val);
    }
}
