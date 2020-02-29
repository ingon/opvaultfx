package dev.ingon.opvaultfx;

import dev.ingon.opvault.Profile;
import dev.ingon.opvault.ProfileException;
import dev.ingon.opvault.ProfileException.ProfileLockedException;
import javafx.event.Event;
import javafx.scene.control.SplitPane;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.BorderPane;
import javafx.util.Duration;

public class UnlockedPane extends BorderPane {
    private final HeaderPane header = new HeaderPane();
    
    private final SplitPane split = new SplitPane();
    private final ListPane list = new ListPane();
    private final DetailsPane details = new DetailsPane();
    
    private final TimerScheduledService lockTimer = new TimerScheduledService();
    private volatile Profile profile;
    
    public UnlockedPane() {
        setId("unlocked");
        
        lockTimer.setDelay(Duration.seconds(1));
        lockTimer.setPeriod(Duration.seconds(1));
        lockTimer.setRestartOnFailure(true);
        lockTimer.valueProperty().addListener((__, ___, d) -> {
            if (d != null && d.toMinutes() >= 15) {
                timeLock();
            }
        });
        
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
        
        addEventFilter(InputEvent.ANY, this::userAlive);
    }
    
    public void show(Profile profile) {
        header.showProfile(profile);
        try {
            list.showProfile(profile);
            
            this.profile = profile;
            userAlive(null);
            lockTimer.restart();
        } catch (ProfileException e) {
            App.showError("Cannot load profile: " + profile.getProfileName(), e);
            fireEvent(ProfileEvent.lock());
        }
    }
    
    public void hide() {
        list.clearProfile();
        header.clearProfile();
        lockTimer.cancel();
        profile = null;
    }
    
    private void timeLock() {
        if (profile == null) {
            return;
        }
        
        profile.lock();
        fireEvent(ProfileEvent.lock());
    }
    
    private void userAlive(Event ev) {
        lockTimer.touch();
    }
}
