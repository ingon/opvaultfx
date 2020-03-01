package dev.ingon.opvaultfx;

import java.awt.AWTException;

import dev.ingon.opvault.ItemSection;
import dev.ingon.opvault.ItemSectionField;
import dev.ingon.opvault.ItemSectionField.Kind;
import dev.ingon.opvault.SecureString;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class ItemSectionPane extends VBox {
    public ItemSectionPane(ItemSection section) {
        getStyleClass().add("item-section");
        
        var itemHeader = new BorderPane();
        itemHeader.getStyleClass().add("item-section-header");
        
        var title = section.getTitle();
        if (title == null || title.isBlank()) {
            title = section.getName();
        }
        if (title != null && !title.isBlank()) {
            var titleLbl = new Label(title);
            titleLbl.getStyleClass().add("item-section-title");
            itemHeader.setCenter(titleLbl);
            BorderPane.setAlignment(titleLbl, Pos.CENTER_LEFT);
        }
        
        if (section.getFields() != null) {
            ItemSectionField user = null;
            ItemSectionField pass = null;
            
            for (var f : section.getFields()) {
                if (f.getValue() == null) {
                    continue;
                }
                
                var titleLbl = new Label(f.getTitle());
                titleLbl.getStyleClass().add("item-section-field-header");
                getChildren().add(titleLbl);
                
                if (f.getKind() == Kind.Concealed) {
                    if (f.getName() != null && f.getName().startsWith("TOTP_")) {
                        getChildren().add(new TOTPPane((SecureString) f.getValue()));
                    } else {
                        getChildren().add(new PasswordPane((SecureString) f.getValue()));
                        pass = f;
                    }
                } else {
                    var txt = new TextField();
                    txt.setText(f.getValue().toString());
                    txt.setEditable(false);
                    getChildren().add(txt);
                    
                    if ("username".equals(f.getTitle())) {
                        user = f;
                    } if (f.getKind() == Kind.Email && user == null) {
                        user = f;
                    }
                }
            }
            
            if (user != null && pass != null) {
                var type = new Button("Auto type");
                type.getStyleClass().add("item-section-type");
                itemHeader.setRight(type);
                
                var userf = user;
                var passf = pass;
                type.setOnAction((____) -> {
                    try {
                        KeyboardRobot r = new KeyboardRobot();
                        r.focusPreviousApp();
                        
                        r.type(userf.getValue().toString());
                        r.focusNextField();
                        r.type((SecureString) passf.getValue());
                        r.submit();
                    } catch (AWTException e) {
                        App.showError("Cannot auto type", e);
                    }
                });
            }
        }
        
        if (! itemHeader.getChildren().isEmpty()) {
            getChildren().add(0, itemHeader);
        }
    }
}
