package org.abpass.ui;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.abpass.opvault.Profile;
import org.abpass.opvault.ProfileException;
import org.abpass.opvault.SecureString;
import org.abpass.opvault.Security;
import org.abpass.opvault.Vault;
import org.abpass.opvault.VaultException;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;

public class LockedPane extends VBox {
    private final Label headerLbl = new Label("ABPass");
    
    private final Label vaultLbl = new Label("Vault: ");
    private final Label vaultValue = new Label();
    private final Label profileLbl = new Label("Profile: ");
    private final Label profileValue = new Label();
    private final Button changeBtn = new Button("Change");
    
    private final SecurePasswordField passwordFld = new SecurePasswordField();
    private final Button unlockBtn = new Button("Unlock");
    
    private static Settings settings = Settings.load();
    
    private Vault vault;
    private Profile profile;
    
    public LockedPane() throws Exception {
        if (settings != null) {
            vault = new Vault(Paths.get(settings.getVault()));
            profile = vault.getProfile(settings.getProfile());
        }
        
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
        
        if (vault != null) {
            vaultValue.setText(splitPath(vault.path));
            profileValue.setText(profile.getProfileName());
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
        if (passwordFld.text == null) {
            return;
        }
        
        try {
            profile.unlock(passwordFld.text);
            passwordFld.reset(null);
            fireEvent(ProfileEvent.unlock(profile));
            
            if (settings == null) {
                settings = new Settings();
            }
            
            settings.setVault(this.vault.path.toString());
            settings.setProfile(profile.getProfileName());
            settings.save();
        } catch(ProfileException e) {
            passwordFld.reset(e.getMessage());
        }
    }
    
    private void chooseVault(ActionEvent ae) {
        var chooser = new DirectoryChooser();
        chooser.setTitle("Select vault");
        var vaultLoc = chooser.showDialog(getParent().getScene().getWindow());
        if (vaultLoc == null) {
            new Alert(AlertType.ERROR, "No directory selected. \n\nSelect a valid 1password directory").show();
            return;
        }
        
        try {
            var vault = new Vault(vaultLoc.toPath());
            var profiles = vault.getProfileNames();
            
            if (profiles.isEmpty()) {
                new Alert(AlertType.ERROR, "No profiles found. \n\nSelect a valid 1password directory").show();
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
                    new Alert(AlertType.ERROR, "No profile selected. \n\nTo continue select a profile").show();
                    return;
                }
                
                this.profile = vault.getProfile(selected.get());
                this.vault = vault;
            }
            
            vaultValue.setText(splitPath(this.vault.path));
            profileValue.setText(profile.getProfileName());
        } catch (VaultException | ProfileException e) {
            new Alert(AlertType.ERROR, "Exception: " + e.getLocalizedMessage()).show();
        }
    }
    
    private static class SecurePasswordField extends Control {
        private SecureString text = null;
        
        private final SimpleIntegerProperty len = new SimpleIntegerProperty(this, "len", 0);
        private final SimpleStringProperty error = new SimpleStringProperty(this, "error");
        
        public SecurePasswordField() {
            setFocusTraversable(true);
            setFocused(true);
            
            setOnKeyTyped((e) -> {
                if (KeyEvent.CHAR_UNDEFINED.equals(e.getCharacter())) {
                    return;
                }
                
                char[] data = e.getCharacter().toCharArray();
                if (data.length != 1) {
                    throw new UnsupportedOperationException("no idea");
                }
                
                switch (data[0]) {
                case 8: 
                case 127:
                    delete();
                    break;
                case 9:
                    break;
                case 13:
                    fireEvent(new ActionEvent());
                    break;
                default:
                    append(data);
                }
            });
        }
        
        private void reset(String errorStr) {
            error.set(errorStr);
            text.close();
            text = null;
            len.set(0);
        }
        
        private void delete() {
            if (text != null) {
                text = text.delete(1);
                if (text == null) {
                    len.set(0);
                } else {
                    len.set(len.get() - 1);
                }
            }
        }
        
        private void append(char[] data) {
            if (error.get() != null) {
                error.set(null);
            }
            
            text = text == null ? new SecureString(data) : text.append(data);
            len.set(len.get() + 1);
            Security.wipe(data);
        }
        
        @Override
        protected Skin<?> createDefaultSkin() {
            return new SecurePasswordFieldSkin(this);
        }
    }

    private static class SecurePasswordFieldSkin extends SkinBase<SecurePasswordField> {
        private final Label pass = new Label();
        
        protected SecurePasswordFieldSkin(SecurePasswordField control) {
            super(control);
            
            getChildren().add(pass);
            
            control.len.addListener((__, ___, len) -> {
                StringBuilder sb = new StringBuilder();
                for (int i = 0, n = len.intValue(); i < n; i++) {
                    sb.append("\u25CF");
                }
                pass.setText(sb.toString());
            });
        }
    }
}
