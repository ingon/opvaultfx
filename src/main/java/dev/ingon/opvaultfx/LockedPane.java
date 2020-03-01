package dev.ingon.opvaultfx;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;

import dev.ingon.opvault.Profile;
import dev.ingon.opvault.ProfileException;
import dev.ingon.opvault.ProfileException.InvalidPasswordException;
import dev.ingon.opvault.Vault;
import dev.ingon.opvault.VaultException;
import dev.ingon.opvault.VaultException.VaultProfilesException;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;

public class LockedPane extends VBox {
    private final Label headerLbl = new Label("OPVaultFX");
    
    private final Label vaultLbl = new Label("Vault: ");
    private final Label vaultValue = new Label();
    private final Label profileLbl = new Label("Profile: ");
    private final Label profileValue = new Label();
    private final Button changeBtn = new Button("Change");
    
    private final SecureStringField passwordFld = new SecureStringField();
    private final Button unlockBtn = new Button("Unlock");
    
    private static Settings settings;
    
    private Vault vault;
    private Profile profile;
    
    public LockedPane() {
        setId("locked");
        
        GridPane center = new GridPane();
        center.setId("locked-center");
        getChildren().add(center);
        
        var c1const = new ColumnConstraints(50, 100, 150);
        c1const.setHgrow(Priority.NEVER);
        var c2const = new ColumnConstraints(100, 150, Double.MAX_VALUE);
        c2const.setHgrow(Priority.ALWAYS);
        center.getColumnConstraints().addAll(c1const, c2const);
        
        headerLbl.setId("locked-header");
        center.add(headerLbl, 0, 0, 3, 1);
        GridPane.setHalignment(headerLbl, HPos.CENTER);
        
        vaultLbl.setId("locked-vault-lbl");
        center.add(vaultLbl, 0, 1);
        GridPane.setHalignment(vaultLbl, HPos.RIGHT);
        GridPane.setValignment(vaultLbl, VPos.TOP);
        
        vaultValue.setId("locked-vault");
        center.add(vaultValue, 1, 1, 2, 1);
        
        profileLbl.setId("locked-profile-lbl");
        center.add(profileLbl, 0, 2);
        GridPane.setHalignment(profileLbl, HPos.RIGHT);
        
        profileValue.setId("locked-profile");
        center.add(profileValue, 1, 2);
        
        changeBtn.setId("locked-change");
        center.add(changeBtn, 2, 2);
        GridPane.setHalignment(changeBtn, HPos.RIGHT);
        
        passwordFld.setId("locked-password");
        center.add(passwordFld, 0, 3, 2, 1);
        GridPane.setHalignment(passwordFld, HPos.LEFT);
        
        unlockBtn.setId("locked-unlock");
        center.add(unlockBtn, 2, 3);
        
        passwordFld.addEventHandler(ActionEvent.ACTION, this::act);
        changeBtn.setOnAction(this::chooseVault);
        unlockBtn.setOnAction(this::act);
    }
    
    public void show() {
        passwordFld.requestFocus();
        
        try {
            settings = Settings.load();
            if (settings == null) {
                return;
            }
            
            vault = new Vault(Paths.get(settings.getVault()));
            vaultValue.setText(splitPath(vault.path));
            
            profile = vault.getProfile(settings.getProfile());
            profileValue.setText(profile.path.getFileName().toString());
        } catch (SettingsException e) {
            App.showError("Cannot load settings", e);
        } catch (VaultException e) {
            App.showError("Cannot load vault", e);
            settings = null;
        } catch (ProfileException e) {
            App.showError("Cannot load profile", e);
            settings = null;
        }
    }
    
    private String splitPath(Path input) {
        String sep = FileSystems.getDefault().getSeparator();
        
        StringBuilder result = new StringBuilder();
        
        StringBuilder line = new StringBuilder();
        for (var p : input) {
            String part = p.toString();
            if (line.length() + part.length() > 35) {
                if (result.length() > 0) {
                    result.append(" ");
                }
                result.append(line).append("\n");
                line.setLength(0);
            }
            line.append(sep).append(part);
        }
        
        if (line.length() > 0) {
            if (result.length() > 0) {
                result.append(" ");
            }
            result.append(line);
        }
        
        return result.toString();
    }
    
    private void act(ActionEvent ev) {
        if (passwordFld.isEmpty()) {
            return;
        }
        
        try {
            profile.unlock(passwordFld.get());
            passwordFld.reset(null);
            fireEvent(ProfileEvent.unlock(profile));
            
            if (settings == null) {
                settings = new Settings();
            }
            
            settings.setVault(this.vault.path.toString());
            settings.setProfile(this.profile.path.getFileName().toString());
            settings.save();
        } catch(InvalidPasswordException e) {
            passwordFld.reset("Wrong password");
        } catch (SettingsException e) {
            App.showError("Cannot save settings", e);
        }
    }
    
    private void chooseVault(ActionEvent ae) {
        var chooser = new DirectoryChooser();
        chooser.setTitle("Select vault");
        var vaultLoc = chooser.showDialog(getScene().getWindow());
        if (vaultLoc == null) {
            App.showError("No vault selected", "Select a valid 1password vault to continue");
            return;
        }
        
        try {
            var vault = new Vault(vaultLoc.toPath());
            var profiles = vault.getProfileNames();
            
            if (profiles.isEmpty()) {
                App.showError("No profiles found", "Select a different 1password vault to continue");
                return;
            } else if (profiles.size() == 1) {
                this.profile = vault.getProfile(profiles.get(0));
                this.vault = vault;
            } else {
                var choice = new ChoiceDialog<String>(profiles.get(0), profiles);
                choice.setTitle("Select profile");
                choice.setContentText("Choose profile");
                var selected = choice.showAndWait();
                if (selected.isEmpty()) {
                    App.showError("No profile selected", "Select a profile to continue");
                    return;
                }
                
                this.profile = vault.getProfile(selected.get());
                this.vault = vault;
            }
            
            vaultValue.setText(splitPath(this.vault.path));
            profileValue.setText(this.profile.path.getFileName().toString());
            passwordFld.requestFocus();
        } catch (VaultProfilesException e) {
            App.showError("Cannot load vault profiles", e);
        } catch (VaultException e) {
            App.showError("Cannot load vault", e);
        } catch (ProfileException e) {
            App.showError("Cannot load profile", e);
        }
    }
}
