package dev.ingon.opvault;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import dev.ingon.opvault.ProfileException.ProfileFormatException;
import dev.ingon.opvault.ProfileException.ProfileNotFileException;
import dev.ingon.opvault.ProfileException.ProfileNotFoundException;
import dev.ingon.opvault.ProfileException.ProfileReadException;
import dev.ingon.opvault.VaultException.VaultNotDirectoryException;
import dev.ingon.opvault.VaultException.VaultNotFoundException;
import dev.ingon.opvault.VaultException.VaultProfilesException;

public class Vault {
    public final Path path;
    
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
