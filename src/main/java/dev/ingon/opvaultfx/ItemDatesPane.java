package dev.ingon.opvaultfx;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

import dev.ingon.opvault.Item;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class ItemDatesPane extends VBox {
    private final Label createdLabel = new Label("Created at");
    private final TextField createdField = new TextField();
    
    private final Label updatedLabel = new Label("Updated at");
    private final TextField updatedField = new TextField();
    
    public ItemDatesPane(ObservableValue<Item> observableItem) {
        getStyleClass().add("item-dates");

        createdField.setEditable(false);
        updatedField.setEditable(false);
        
        getChildren().addAll(createdLabel, createdField, updatedLabel, updatedField);

        observableItem.addListener((__, ___, item) -> {
            createdField.setText("");
            updatedField.setText("");
            
            if (item == null) {
                return;
            }
            
            var formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withZone(ZoneId.systemDefault());
            createdField.setText(formatter.format(item.getCreated()));
            updatedField.setText(formatter.format(item.getUpdated()));
        });
    }
}
