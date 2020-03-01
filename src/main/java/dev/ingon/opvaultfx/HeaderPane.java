package dev.ingon.opvaultfx;

import dev.ingon.opvault.Profile;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;

public class HeaderPane extends StackPane {
    private final AnchorPane internal = new AnchorPane();
    private final TextField search = new TextField();
    private final Button lock = new Button("Lock");
    
    private Profile profile;
    
    public HeaderPane() {
        setId("header");
        
        internal.setId("header-internal");
        getChildren().add(internal);
        
        search.setId("header-search");
        search.setPromptText("search");
        AnchorPane.setLeftAnchor(search, 0.);
        
        lock.setId("header-lock");
        lock.setOnAction((ev) -> {
            profile.lock();
            fireEvent(ProfileEvent.lock());  
        });
        AnchorPane.setRightAnchor(lock, 0.);
        
        internal.getChildren().addAll(search, lock);
    }
    
    public void showProfile(Profile profile) {
        this.profile = profile;
    }
    
    public void clearProfile() {
        this.profile = null;
        search.setText("");
    }
    
    public ReadOnlyStringProperty searchTextProperty() {
        return search.textProperty();
    }
}
