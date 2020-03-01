package dev.ingon.opvaultfx;

import java.awt.AWTException;

import dev.ingon.opvault.ItemField;
import dev.ingon.opvault.ItemField.Designation;
import dev.ingon.opvault.ItemField.Type;
import javafx.beans.value.ObservableListValue;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class ItemFieldsPane extends VBox {
    public ItemFieldsPane(ObservableListValue<ItemField> observableFields) {
        getStyleClass().add("item-section");

        observableFields.addListener((__, ___, fields) -> {
            getChildren().clear();
            
            ItemField user = null;
            ItemField pass = null;

            for (var f : fields) {
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
                
                if (f.getDesignation() == Designation.Password) {
                    pass = f;
                } else if (f.getType() == Type.Password && pass == null) {
                    pass = f;
                }
                
                if (f.getDesignation() == Designation.Username) {
                    user = f;
                } else if (f.getType() == Type.Email && user == null) {
                    user = f;
                }
            }
            
            if (user != null && pass != null) {
                var typePane = new BorderPane();
                getChildren().add(0, typePane);
                
                var type = new Button("Auto type");
                type.getStyleClass().add("item-section-type");
                typePane.setRight(type);
                
                var userf = user;
                var passf = pass;
                type.setOnAction((____) -> {
                    try {
                        KeyboardRobot r = new KeyboardRobot();
                        r.focusPreviousApp();
                        
                        r.type(userf.getValue());
                        r.focusNextField();
                        r.type(passf.getValue());
                        r.submit();
                    } catch (AWTException e) {
                        App.showError("Cannot auto type", e);
                    }
                });
            }
        });
    }
}
