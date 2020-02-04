package org.abpass.opvault;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.abpass.opvault.ProfileException.ProfileFormatException;
import org.abpass.opvault.ProfileException.ProfileNotFileException;
import org.abpass.opvault.ProfileException.ProfileNotFoundException;
import org.abpass.opvault.ProfileException.ProfileReadException;
import org.abpass.opvault.VaultException.VaultNotDirectoryException;
import org.abpass.opvault.VaultException.VaultNotFoundException;
import org.abpass.opvault.VaultException.VaultProfilesException;

public class Vault {
    public final Path path;
    
    public static Vault dropbox() throws VaultNotFoundException, VaultNotDirectoryException {
        var p = Paths.get(System.getProperty("user.home"), "Dropbox", "1Password.opvault");
        return new Vault(p);
    }
    
    public Vault(Path path) throws VaultNotFoundException, VaultNotDirectoryException {
        if (! Files.exists(path)) {
            throw new VaultNotFoundException(path);
        }
        
        if (! Files.isDirectory(path)) {
            throw new VaultNotDirectoryException(path);
        }
        
        this.path = path;
    }
    
    public List<String> getProfileNames() throws VaultProfilesException {
        try (var files = Files.list(path)) {
            return files.filter((p) -> {
                if (! Files.isDirectory(p)) {
                    return false;
                }
                
                var profilePath = p.resolve("profile.js");
                return Files.isRegularFile(profilePath);
            }).map((p) -> {
                return p.getFileName().toString();
            }).collect(Collectors.toList());
        } catch (IOException exc) {
            throw new VaultProfilesException(path, exc);
        }
    }
    
    public Profile getDefaultProfile() throws ProfileNotFoundException, ProfileNotFileException, ProfileFormatException, ProfileReadException {
        return getProfile("default");
    }
    
    public Profile getProfile(String name) throws ProfileNotFoundException, ProfileNotFileException, ProfileFormatException, ProfileReadException {
        return new Profile(this, path.resolve(name));
    }
}
