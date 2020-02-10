package org.abpass.ui;

import org.abpass.opvault.Profile;
import org.abpass.opvault.ProfileException.InvalidPasswordException;
import org.abpass.opvault.SecureString;
import org.abpass.opvault.Security;
import org.abpass.opvault.Vault;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class LockedPane extends VBox {
    public final Vault vault;
    public final Profile profile;
    public final SimpleBooleanProperty unlocked = new SimpleBooleanProperty(this, "locked", false);
    
    private final SecurePasswordField passwordFld;
    
    public LockedPane(Vault vault, Profile profile) {
        this.vault = vault;
        this.profile = profile;
        
        setId("locked");
        
        VBox center = new VBox();
        center.setId("locked-center");
        
        BorderPane passwordRow = new BorderPane();
        passwordRow.setId("locked-password");
        center.getChildren().add(passwordRow);
        
        passwordFld = new SecurePasswordField();
        passwordRow.setCenter(passwordFld);
        
        var unlockBtn = new Button("Unlock");
        unlockBtn.setId("locked-unlock");
        passwordRow.setRight(unlockBtn);
        
        getChildren().add(center);
        
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
            passwordFld.error.set(e.getMessage());
        } finally {
            passwordFld.text.close();
            passwordFld.text = null;
            passwordFld.len.set(0);
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
            ctx.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            
            ctx.setFill(Color.web("#fefefe"));
            if (len == 0) {
                ctx.setFont(Font.font(20));
                if (getSkinnable().error.get() != null) {
                    ctx.setFill(Color.web("#CF6679"));
                    ctx.fillText(getSkinnable().error.get(), 8, 24);
                } else {
                    ctx.fillText("Master password", 8, 24);
                }
            } else {
                for (int i = 0; i < len; i++) {
                    ctx.fillOval(i * 32 + 8, 8, 24, 24);
                }
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
