package org.abpass.opvault;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.abpass.opvault.Exceptions.ProfileException;
import org.abpass.opvault.Exceptions.VaultException;
import org.abpass.opvault.Exceptions.VaultNotDirectoryException;
import org.abpass.opvault.Exceptions.VaultNotFoundException;

public class Vault {
    public final Path path;
    
    public static Vault dropbox() throws VaultException {
        var p = Paths.get(System.getProperty("user.home"), "Dropbox", "1Password.opvault");
        return new Vault(p);
    }
    
    public Vault(Path path) throws VaultException {
        if (! Files.exists(path)) {
            throw new VaultNotFoundException(path);
        }
        
        if (! Files.isDirectory(path)) {
            throw new VaultNotDirectoryException(path);
        }
        
        this.path = path;
    }
    
    public List<String> getProfileNames() throws IOException {
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
        }
    }
    
    public Profile getDefaultProfile() throws ProfileException {
        return getProfile("default");
    }
    
    public Profile getProfile(String name) throws ProfileException {
        return new Profile(this, path.resolve(name));
    }
}
