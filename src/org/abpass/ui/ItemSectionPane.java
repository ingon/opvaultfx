package org.abpass.ui;

import org.abpass.opvault.ItemSection;
import org.abpass.opvault.ItemSectionField.Kind;

import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
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
            titleLbl.getStyleClass().add("item-section-header");
            getChildren().add(titleLbl);
        }
        
        if (section.getFields() != null) {
            for (var f : section.getFields()) {
                if (f.getValue() == null) {
                    continue;
                }
                
                String v = f.getValue().toString();
                var titleLbl = new Label(f.getTitle());
                titleLbl.getStyleClass().add("item-section-field-header");
                getChildren().add(titleLbl);
                if (f.getKind() == Kind.Concealed) {
                    var pwd = new PasswordField();
                    pwd.setText(v);
                    pwd.setEditable(false);
                    getChildren().add(pwd);
                } else {
                    var txt = new TextField(v);
                    txt.setEditable(false);
                    getChildren().add(txt);
                }
            }
        }
    }
}
