package dev.ingon.opvaultfx;

import java.io.IOException;

import dev.ingon.opvault.ItemException;
import dev.ingon.opvault.Profile;
import dev.ingon.opvault.ProfileException;
import dev.ingon.opvault.ProfileException.ProfileLockedException;
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
        
        list.itemProperty().addListener((__, ___, item) -> {
            try {
                if (item == null) {
                    details.clearItem();
                } else {
                    details.showItem(item.getItem(), item.getOverview());
                }
            } catch (ProfileLockedException e) {
                e.printStackTrace();
            } catch (ItemException e) {
                e.printStackTrace();
            }
        });
    }
    
    public void showProfile(Profile profile) {
        try {
            header.showProfile(profile);
            list.showProfile(profile);
        } catch (ProfileException | ItemException e) {
            e.printStackTrace();
            fireEvent(ProfileEvent.lock());
        }
    }
    
    public void clearProfile() {
        list.clearProfile();
        header.clearProfile();
    }
}
