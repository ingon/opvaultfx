package dev.ingon.opvaultfx;

import dev.ingon.opvault.ItemSection;
import dev.ingon.opvault.SecureString;
import dev.ingon.opvault.ItemSectionField.Kind;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class ItemSectionPane extends VBox {
    public ItemSectionPane(ItemSection section) {
        getStyleClass().add("item-section");
        
        var title = section.getTitle();
        if (title == null || title.isBlank()) {
            title = section.getName();
        }
        if (title != null && !title.isBlank()) {
            var titleLbl = new Label(title);
            titleLbl.getStyleClass().add("item-section-title");
            getChildren().add(titleLbl);
        }
        
        if (section.getFields() != null) {
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
                    }
                } else {
                    var txt = new TextField();
                    txt.setText(f.getValue().toString());
                    txt.setEditable(false);
                    getChildren().add(txt);
                }
            }
        }
    }
}