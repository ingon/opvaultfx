package org.abpass.ui;

import org.abpass.opvault.ItemDetail;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

public class ItemNotesPane extends VBox {
    private final TextArea text = new TextArea();
    
    public ItemNotesPane(ObservableValue<ItemDetail> observableDetail) {
        getStyleClass().add("item-fields");
        
        text.setEditable(false);
        
        getChildren().add(text);

        observableDetail.addListener((__, ___, detail) -> {
            text.setText("");
            if (detail == null || detail.getNotes() == null) {
                return;
            }
            
            text.setText(detail.getNotes());
        });
    }
}
