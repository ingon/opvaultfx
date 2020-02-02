package org.abpass.ui;

import org.abpass.opvault.Exceptions.InvalidPasswordException;
import org.abpass.opvault.Profile;
import org.abpass.opvault.SecureString;
import org.abpass.opvault.Security;
import org.abpass.opvault.Vault;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.ActionEvent;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class LockedPane extends BorderPane {
    
    public final Vault vault;
    public final Profile profile;
    public final SimpleBooleanProperty unlocked = new SimpleBooleanProperty(this, "locked", false);
    
    private final SecurePasswordField passwordFld;
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
        
        passwordFld = new SecurePasswordField();
        center.getChildren().add(passwordFld);
        
        var unlockBtn = new Button("Unlock");
        center.getChildren().add(unlockBtn);
        
        errorLbl = new Label("");
        center.getChildren().add(errorLbl);
        
        setCenter(center);
        
        passwordFld.addEventHandler(ActionEvent.ACTION, this::act);
        unlockBtn.setOnAction(this::act);
    }
    
    private void act(ActionEvent ev) {
        if (passwordFld.text == null) {
            return;
        }
        
        try {
            profile.unlock(passwordFld.text);
            unlocked.setValue(true);
        } catch (InvalidPasswordException e) {
            errorLbl.setText(e.getMessage());
        } finally {
            passwordFld.text.close();
            passwordFld.text = null;
            passwordFld.len.set(0);
        }
    }
    
    private static class SecurePasswordField extends Control {
        private SecureString text = null;
        private final SimpleIntegerProperty len = new SimpleIntegerProperty(this, "len", 0);
        
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
        private Canvas canvas;
        
        protected SecurePasswordFieldSkin(SecurePasswordField control) {
            super(control);
            
            canvas = new Canvas(400, 40); // update with font metrics
            getChildren().add(canvas);

            redraw(0);
            control.len.addListener((source, oldValue, newValue) -> {
                redraw(newValue.intValue());
            });
        }
        
        private void redraw(int len) {
            var ctx = canvas.getGraphicsContext2D();
            ctx.setFill(Color.web("#fefefe"));
            ctx.fillRoundRect(0, 0, canvas.getWidth(), canvas.getHeight(), 8, 8);
            
            ctx.setStroke(Color.web("#282828"));
            ctx.strokeRoundRect(0, 0, canvas.getWidth(), canvas.getHeight(), 8, 8);
            
            for (int i = 0; i < len; i++) {
                ctx.setFill(Color.web("#202020"));
                ctx.fillOval(i * 32 + 8, 8, 24, 24);
            }
        }
        
        @Override
        protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
            return canvas.getHeight() + topInset + bottomInset;
        }
        
        @Override
        protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
            return canvas.getWidth() + leftInset + rightInset;
        }
    }
}
