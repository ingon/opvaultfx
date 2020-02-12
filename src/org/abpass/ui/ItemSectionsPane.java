package org.abpass.ui;

import org.abpass.opvault.ItemSection;

import javafx.beans.value.ObservableListValue;
import javafx.scene.layout.VBox;

public class ItemSectionsPane extends VBox {
    public ItemSectionsPane(ObservableListValue<ItemSection> sections) {
        getStyleClass().add("item-sections");

        sections.addListener((__, ___, newValue) -> {
            getChildren().clear();

            for (var s : newValue) {
                getChildren().add(new ItemSectionPane(s));
            }
        });
    }
}
