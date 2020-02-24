package dev.ingon.opvaultfx;

import dev.ingon.opvault.ItemDetail;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

public class ItemNotesPane extends VBox {
    private final TextArea text = new TextArea();
    
    public ItemNotesPane(ObservableValue<ItemDetail> observableDetail) {
        getStyleClass().add("item-section");
        
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
