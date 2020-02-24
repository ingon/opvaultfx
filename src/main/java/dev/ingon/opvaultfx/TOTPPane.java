package dev.ingon.opvaultfx;

import java.net.URI;

import dev.ingon.opvault.SecureString;
import dev.ingon.otp.TOTPGenerator;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.BorderPane;

public class TOTPPane extends BorderPane {
    private final TOTPGenerator generator;
    
    private final ProgressIndicator progress;
    private final Label value;
    private final Button copy;
    
    public TOTPPane(SecureString secret) {
        this.generator = secret.apply((chs) -> {
            String can = new String(chs).replace("\\/", "/");
            URI uri = URI.create(can);
            return TOTPGenerator.fromURI(uri);
        });
        
        setLeft(progress = new ProgressIndicator());
        setCenter(value = new Label());
        setRight(copy = new Button("Copy"));
        
        TOTPTask task = new TOTPTask(generator);
        
        progress.progressProperty().bind(task.progressProperty());
        value.textProperty().bind(task.messageProperty());
        copy.setOnAction((ev) -> {
            var clipboard = Clipboard.getSystemClipboard();
            var content = new ClipboardContent();
            content.putString(task.getMessage());
            clipboard.setContent(content);
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
        protected Void call() throws Exception {
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
