package org.abpass.ui;

import java.io.IOException;

import org.abpass.opvault.ItemException;
import org.abpass.opvault.Profile;
import org.abpass.opvault.ProfileException;
import org.abpass.opvault.ProfileException.ProfileLockedException;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.SplitPane;

public class UnlockedPane extends SplitPane {
    private final ListPane list = new ListPane();
    private final DetailsPane details = new DetailsPane();
    
    private ObjectProperty<Profile> profile = new SimpleObjectProperty<Profile>(this, "profile");
    
    public UnlockedPane() throws IOException {
        setId("unlocked");
        
        getItems().add(list);
        SplitPane.setResizableWithParent(list, false);
        
        getItems().add(details);
        SplitPane.setResizableWithParent(details, false);
        
        setDividerPositions(0.35);
        
        list.itemProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                var item = list.getItem();
                try {
                    if (item == null) {
                        details.showItem(null, null);
                    } else {
                        details.showItem(item.item, item.overview);
                    }
                } catch (ProfileLockedException e) {
                    e.printStackTrace();
                } catch (ItemException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    
    public void setProfile(Profile profile) throws ProfileException, ItemException {
        this.profile.setValue(profile);
        
        this.list.showProfile(profile);
    }
}
