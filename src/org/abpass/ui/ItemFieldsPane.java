package org.abpass.ui;

import org.abpass.opvault.ItemField;
import org.abpass.opvault.ItemField.Designation;
import org.abpass.opvault.ItemField.Type;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableListValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class ItemFieldsPane extends VBox {
    public ItemFieldsPane(ObservableListValue<ItemField> fields) {
        getStyleClass().add("item-fields");
        
        fields.addListener(new ChangeListener<ObservableList<ItemField>>() {
            @Override
            public void changed(ObservableValue<? extends ObservableList<ItemField>> observable,
                    ObservableList<ItemField> oldValue, ObservableList<ItemField> newValue) {
                getChildren().clear();
                
                for (var f : newValue) {
                    getChildren().add(new Label(f.getName()));
                    if (f.getDesignation() == Designation.Password || f.getType() == Type.Password) {
                        var pwd = new PasswordField();
                        pwd.setText(f.getValue());
                        pwd.setEditable(false);
                        getChildren().add(pwd);
                    } else {
                        var txt = new TextField(f.getValue());
                        txt.setEditable(false);
                        getChildren().add(txt);
                    }
                }
            }});
    }
}
