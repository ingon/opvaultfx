package dev.ingon.opvaultfx;

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
    
    public UnlockedPane() {
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
                App.showError("Profile already locked", e);
                fireEvent(ProfileEvent.lock());
            }
        });
    }
    
    public void showProfile(Profile profile) {
        header.showProfile(profile);
        try {
            list.showProfile(profile);
        } catch (ProfileException e) {
            App.showError("Cannot load profile: " + profile.getProfileName(), e);
            fireEvent(ProfileEvent.lock());
        }
    }
    
    public void clearProfile() {
        list.clearProfile();
        header.clearProfile();
    }
}
