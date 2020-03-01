package dev.ingon.opvaultfx;

import java.awt.AWTException;
import java.net.URI;

import dev.ingon.opvault.SecureString;
import dev.ingon.otp.TOTPGenerator;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class TOTPPane extends HBox {
    private final TOTPGenerator generator;
    
    private final ProgressIndicator progress = new ProgressIndicator();
    private final Label value = new Label();
    private final Button copy = new Button("Copy");
    private final Button type = new Button("Type");
    
    public TOTPPane(SecureString secret) {
        getStyleClass().add("totp-pane");
        
        this.generator = secret.apply((chs) -> {
            String can = new String(chs).replace("\\/", "/");
            URI uri = URI.create(can);
            return TOTPGenerator.fromURI(uri);
        });
        
        var space = new Region();

        getChildren().addAll(progress, value, space, copy, type);
        HBox.setHgrow(progress, Priority.NEVER);
        HBox.setHgrow(value, Priority.NEVER);
        HBox.setHgrow(space, Priority.ALWAYS);
        HBox.setHgrow(copy, Priority.NEVER);
        HBox.setHgrow(type, Priority.NEVER);
        
        TOTPTask task = new TOTPTask(generator);
        
        progress.progressProperty().bind(task.progressProperty());
        value.textProperty().bind(task.messageProperty());
        
        copy.setOnAction((__) -> {
            var clipboard = Clipboard.getSystemClipboard();
            var content = new ClipboardContent();
            content.putString(task.getMessage());
            clipboard.setContent(content);
        });
        
        type.setOnAction((__) -> {
            try {
                KeyboardRobot robot = new KeyboardRobot();
                robot.focusPreviousApp();
                robot.type(task.messageProperty().get());
            } catch (AWTException e) {
                App.showError("Cannot auto type", e);
            }
        });
        
        var th = new Thread(task);
        th.setDaemon(true);
        th.start();
    }
    
    private static class TOTPTask extends Task<Void> {
        private final TOTPGenerator generator;
        
        public TOTPTask(TOTPGenerator generator) {
            this.generator = generator;
        }
        
        @Override
        protected Void call() throws InterruptedException {
            while (!isCancelled()) {
                updateMessage(generator.generate());
                
                var ts = System.currentTimeMillis() / 1000l;
                var last = ts % 30;
                updateProgress(30 - last, 30);
                Thread.sleep(1000);
            }
            return null;
        }
    }
}
