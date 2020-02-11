package org.abpass.ui;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;

public class HeaderPane extends StackPane {
    private final AnchorPane internal = new AnchorPane();
    private final TextField search = new TextField();
    private final Button lock = new Button("Lock");
    
    public HeaderPane() {
        setId("header");
        
        internal.setId("header-internal");
        getChildren().add(internal);
        
        search.setId("header-search");
        search.setPromptText("search");
        AnchorPane.setLeftAnchor(search, 0.);
        
        lock.setId("header-lock");
        AnchorPane.setRightAnchor(lock, 0.);
        
        internal.getChildren().addAll(search, lock);
    }
    
    public ReadOnlyStringProperty searchTextProperty() {
        return search.textProperty();
    }
}
