package org.abpass.ui;

import org.abpass.opvault.ItemSection;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableListValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.layout.VBox;

public class ItemSectionsPane extends VBox {
    public ItemSectionsPane(ObservableListValue<ItemSection> sections) {
        getStyleClass().add("item-sections");
        
        sections.addListener(new ChangeListener<ObservableList<ItemSection>>() {
            @Override
            public void changed(ObservableValue<? extends ObservableList<ItemSection>> observable,
                    ObservableList<ItemSection> oldValue, ObservableList<ItemSection> newValue) {
                getChildren().clear();
                
                for (var s : newValue) {
                    getChildren().add(new ItemSectionPane(s));
                }
            }
        });
    }
}
