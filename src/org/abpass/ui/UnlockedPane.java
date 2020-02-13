package org.abpass.ui;

import java.io.IOException;

import org.abpass.opvault.ItemException;
import org.abpass.opvault.Profile;
import org.abpass.opvault.ProfileException;
import org.abpass.opvault.ProfileException.ProfileLockedException;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;

public class UnlockedPane extends BorderPane {
    private final HeaderPane header = new HeaderPane();
    
    private final SplitPane split = new SplitPane();
    private final ListPane list = new ListPane();
    private final DetailsPane details = new DetailsPane();
    
    public UnlockedPane() throws IOException {
        setId("unlocked");
        
        setTop(header);
        setCenter(split);
        
        split.setId("unlocked-split");
        split.setDividerPositions(0.35);
        SplitPane.setResizableWithParent(list, false);
        SplitPane.setResizableWithParent(details, false);
        split.getItems().addAll(list, details);
        
        list.searchProperty().bind(header.searchTextProperty());
        
        list.itemProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                var item = list.getItem();
                try {
                    if (item == null) {
                        details.showItem(null, null);
                    } else {
                        details.showItem(item.getItem(), item.getOverview());
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
        this.list.showProfile(profile);
    }
    
    public void clearProfile() {
        this.list.clearProfile();
    }
}
