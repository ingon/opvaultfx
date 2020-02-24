package dev.ingon.opvaultfx;

import dev.ingon.opvault.ItemSection;
import javafx.beans.value.ObservableListValue;
import javafx.scene.layout.VBox;

public class ItemSectionsPane extends VBox {
    public ItemSectionsPane(ObservableListValue<ItemSection> observableSections) {
        getStyleClass().add("item-sections");

        observableSections.addListener((__, ___, sections) -> {
            getChildren().clear();

            for (var s : sections) {
                getChildren().add(new ItemSectionPane(s));
            }
        });
    }
}
