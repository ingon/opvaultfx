package org.abpass.ui;

import org.abpass.opvault.SecureString;

import javafx.event.Event;
import javafx.event.EventType;

public class ProfileEvent extends Event {
    private static final long serialVersionUID = 1L;
    
    public static final EventType<ProfileEvent> UNLOCK = new EventType<ProfileEvent>("UNLOCK");
    
    public static final EventType<ProfileEvent> LOCK = new EventType<ProfileEvent>("LOCK");
    
    public final SecureString password; 
    
    private ProfileEvent(SecureString password, EventType<ProfileEvent> eventType) {
        super(eventType);
        this.password = password;
    }
    
    public static ProfileEvent unlock(SecureString password) {
        return new ProfileEvent(password, UNLOCK);
    }
    
    public static ProfileEvent lock() {
        return new ProfileEvent(null, LOCK);
    }
}
