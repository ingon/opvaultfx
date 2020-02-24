package dev.ingon.opvaultfx;

import dev.ingon.opvault.Profile;
import javafx.event.Event;
import javafx.event.EventType;

public class ProfileEvent extends Event {
    private static final long serialVersionUID = 1L;
    
    public static final EventType<ProfileEvent> UNLOCK = new EventType<ProfileEvent>("UNLOCK");
    
    public static final EventType<ProfileEvent> LOCK = new EventType<ProfileEvent>("LOCK");
    
    public final Profile profile;
    
    private ProfileEvent(EventType<ProfileEvent> eventType, Profile profile) {
        super(eventType);
        this.profile = profile;
    }
    
    public static ProfileEvent unlock(Profile profile) {
        return new ProfileEvent(UNLOCK, profile);
    }
    
    public static ProfileEvent lock() {
        return new ProfileEvent(LOCK, null);
    }
}
