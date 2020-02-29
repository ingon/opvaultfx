package dev.ingon.opvaultfx;

import dev.ingon.opvault.SecureString;
import dev.ingon.opvault.Security;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.input.KeyEvent;

class SecureStringField  extends Control {
    private SecureString text = null;
    
    final SimpleIntegerProperty len = new SimpleIntegerProperty(this, "len", 0);
    final SimpleStringProperty error = new SimpleStringProperty(this, "error");
    
    public SecureStringField() {
        setFocusTraversable(true);
        setFocused(true);
        
        setOnMouseClicked((__) -> {
            requestFocus();
        });
        
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
    
    public boolean isEmpty() {
        return len.get() == 0 || text == null;
    }
    
    protected SecureString get() {
        return text;
    }
    
    public void reset(String errorStr) {
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
        return new SecureStringFieldSkin(this);
    }
}