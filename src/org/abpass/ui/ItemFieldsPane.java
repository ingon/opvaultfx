package org.abpass.ui;

import org.abpass.opvault.ItemField;
import org.abpass.opvault.ItemField.Designation;
import org.abpass.opvault.ItemField.Type;

import javafx.beans.value.ObservableListValue;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class ItemFieldsPane extends VBox {
    public ItemFieldsPane(ObservableListValue<ItemField> fields) {
        getStyleClass().add("item-section");

        fields.addListener((__, ___, newValue) -> {
            getChildren().clear();

            for (var f : newValue) {
                var lbl = new Label(f.getName());
                lbl.getStyleClass().add("item-section-field-header");
                getChildren().add(lbl);

                if (f.getDesignation() == Designation.Password || f.getType() == Type.Password) {
                    getChildren().add(new PasswordPane(f.getValue()));
                } else {
                    var txt = new TextField();
                    f.getValue().accept((chs) -> {
                        txt.setText(new String(chs));
                    });
                    txt.setEditable(false);
                    getChildren().add(txt);
                }
            }
        });
    }
}
