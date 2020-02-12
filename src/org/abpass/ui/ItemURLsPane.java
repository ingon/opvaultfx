package org.abpass.ui;

import java.util.HashSet;

import org.abpass.opvault.ItemOverview;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class ItemURLsPane extends VBox {
    public ItemURLsPane(ObservableValue<ItemOverview> observableOverview) {
        getStyleClass().add("item-section");

        observableOverview.addListener((__, ___, overview) -> {
            getChildren().clear();
            if (overview == null) {
                return;
            }
            
            var dedupe = new HashSet<String>();
            
            if (overview.getUrl() != null) {
                var lbl = new Label("website");
                lbl.getStyleClass().add("item-section-field-header");
                getChildren().add(lbl);
                
                var txt = new TextField();
                txt.setText(overview.getUrl());
                txt.setEditable(false);
                getChildren().add(txt);
                
                dedupe.add(overview.getUrl());
            }
            
            if (overview.getUrls() == null) {
                return;
            }
            
            for (var u : overview.getUrls()) {
                if (dedupe.contains(u.getU())) {
                    continue;
                }
                dedupe.add(u.getU());
                
                String name = u.getL();
                if (name == null || name.isBlank()) {
                    if (dedupe.size() == 1) {
                        name = "website";
                    } else if (dedupe.size() > 1) {
                        name = "website " + (dedupe.size() - 1);
                    }
                }
                
                var lbl = new Label(name);
                lbl.getStyleClass().add("item-section-field-header");
                getChildren().add(lbl);
                
                var txt = new TextField();
                txt.setText(u.getU());
                txt.setEditable(false);
                getChildren().add(txt);
            }
        });
    }
}
