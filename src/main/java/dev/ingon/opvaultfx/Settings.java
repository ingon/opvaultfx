package dev.ingon.opvaultfx;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import dev.ingon.json.zero.ParseException;
import dev.ingon.json.zero.hl.JsonParser;
import dev.ingon.json.zero.hl.JsonTypedHandler;

public class Settings {
    public static Settings load() throws SettingsException {
        Path settingsPath = getSettingsFile();
        if (settingsPath == null || !Files.isRegularFile(settingsPath)) {
            return null;
        }
        
        try {
            String settingsStr = Files.readString(settingsPath);
            return JsonParser.parse(settingsStr.toCharArray(), newParser());
        } catch (IOException e) {
            throw new SettingsException("Cannot read settings file at: " + settingsPath, e);
        } catch (ParseException e) {
            throw new SettingsException("Invalid settings file at: " + settingsPath, e);
        }
    }
    
    static JsonTypedHandler<Settings> newParser() {
        JsonTypedHandler<Settings> parser = new JsonTypedHandler<Settings>(Settings::new);
        
        parser.stringProperty("vault", (t, o) -> t.vault = o);
        parser.stringProperty("profile", (t, o) -> t.profile = o);
        
        return parser;
    }
    
    private String vault;
    private String profile;
    
    public String getVault() {
        return vault;
    }
    
    public void setVault(String vault) {
        this.vault = vault;
    }
    
    public String getProfile() {
        return profile;
    }
    
    public void setProfile(String profile) {
        this.profile = profile;
    }

    public void save() throws SettingsException {
        String settingsStr = String.format("{\"vault\": \"%s\", \"profile\": \"%s\"}", vault, profile);
        
        Path settingsPath = getSettingsFile();
        if (! Files.exists(settingsPath)) {
            Path settingsDir = settingsPath.getParent();
            if (! Files.isDirectory(settingsDir)) {
                try {
                    Files.createDirectories(settingsDir);
                } catch (IOException e) {
                    throw new SettingsException("Cannot create settings directory at: " + settingsDir, e);
                }
            }
        }
        
        try {
            Files.writeString(settingsPath, settingsStr);
        } catch (IOException e) {
            throw new SettingsException("Cannot write settings file at: " + settingsPath, e);
        }
    }
    
    private static Path getSettingsFile() {
        return getSettingsFolder().resolve("config.json");
    }
    
    private static Path getSettingsFolder() {
        var userHome = Paths.get(System.getProperty("user.home"));
        
        String osName = System.getProperty("os.name");
        if (osName.startsWith("Windows")) {
            return userHome.resolve(Path.of("AppData", "Local", "opvaultfx"));
        }
        
        var configRoot = userHome.resolve(".config");
        
        String configHome = System.getenv("XDG_CONFIG_HOME");
        if (configHome != null && !configHome.isBlank()) {
            configRoot = Paths.get(configHome);
        }
        
        return configRoot.resolve("opvaultfx");
    }
}
