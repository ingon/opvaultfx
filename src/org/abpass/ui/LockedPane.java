package org.abpass.ui;

import java.security.GeneralSecurityException;

import org.abpass.opvault.Exceptions.InvalidPasswordException;
import org.abpass.opvault.Profile;
import org.abpass.opvault.Vault;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class LockedPane extends BorderPane {
    
    public final Vault vault;
    public final Profile profile;
    public final SimpleBooleanProperty unlocked = new SimpleBooleanProperty(this, "locked", false);
    
    private final PasswordField passwordFld;
    private final Label errorLbl;
    
    public LockedPane(Vault vault, Profile profile) {
        this.vault = vault;
        this.profile = profile;
        
        setId("locked");
        
        VBox center = new VBox();
        center.setMinSize(200, 200);
        center.setMaxSize(400, 400);
        
        var passwordLbl = new Label("Password: ");
        center.getChildren().add(passwordLbl);
        
        passwordFld = new PasswordField();
        passwordFld.setText("freddy");
        center.getChildren().add(passwordFld);
        
        var unlockBtn = new Button("Unlock");
        center.getChildren().add(unlockBtn);
        
        errorLbl = new Label("");
        center.getChildren().add(errorLbl);
        
        setCenter(center);
        
        passwordFld.setOnAction(this::act);
        unlockBtn.setOnAction(this::act);
    }
    
    private void act(ActionEvent ev) {
        try {
            profile.unlock(passwordFld.getText());
            unlocked.setValue(true);
        } catch (InvalidPasswordException | GeneralSecurityException e) {
            errorLbl.setText(e.getMessage());
        }
    }
}
