package org.abpass.ui;

import org.abpass.opvault.ItemDetail;

import javafx.beans.value.ObservableValue;
import javafx.scene.layout.VBox;

public class ItemPasswordPane extends VBox {
    public ItemPasswordPane(ObservableValue<ItemDetail> observableDetail) {
        getStyleClass().add("item-fields");
        
        observableDetail.addListener((__, ___, detail) -> {
            getChildren().clear();
            if (detail == null || detail.getPassword() == null) {
                return;
            }
            getChildren().add(new PasswordPane(detail.getPassword()));
        });
    }
}
