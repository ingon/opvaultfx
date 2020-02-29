package dev.ingon.opvaultfx;

import java.time.Duration;
import java.time.Instant;

import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;

public class TimerScheduledService extends ScheduledService<Duration> {
    private Instant lastUpdate;
    
    @Override
    protected Task<Duration> createTask() {
        return new Task<Duration>() {
            @Override
            protected Duration call() throws Exception {
                return Duration.between(lastUpdate, Instant.now());
            }
        };
    }
    
    public void touch() {
        lastUpdate = Instant.now();
    }
}
