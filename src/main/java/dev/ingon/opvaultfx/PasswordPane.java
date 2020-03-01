package dev.ingon.opvaultfx;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;

import dev.ingon.opvault.SecureString;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class PasswordPane extends HBox {
    public PasswordPane(SecureString data) {
        getStyleClass().add("password-pane");
        
        var pwd = new PasswordField();
        pwd.getStyleClass().add("password-text");
        pwd.setText("use the copy button");
        pwd.setEditable(false);

        var copy = new Button("Copy");
        copy.getStyleClass().add("password-copy");
        copy.setOnAction((e) -> {
            var clipboard = Clipboard.getSystemClipboard();
            var content = new ClipboardContent();
            data.accept((chars) -> {
                content.putString(new String(chars));
                clipboard.setContent(content);
            });
        });
        
        var fill = new Button("Fill");
        fill.getStyleClass().add("password-fill");
        fill.setOnAction((__) -> {
            try {
                Robot r = new Robot();
                r.setAutoWaitForIdle(true);
                
                r.keyPress(KeyEvent.VK_ALT);
                r.keyPress(KeyEvent.VK_TAB);
                r.delay(50);
                r.keyRelease(KeyEvent.VK_TAB);
                r.keyRelease(KeyEvent.VK_ALT);
                
                data.accept((chars) -> {
                    KeyboardChar[] combos = new KeyboardChar[chars.length];
                    for (int i = 0; i < chars.length; i++) {
                        combos[i] = KeyboardChar.get(chars[i]);
                    }
                    
                    for (int i = 0; i < combos.length; i++) {
                        combos[i].type(r);
                        combos[i] = null;
                    }
                });
            } catch (AWTException e) {
                e.printStackTrace();
            }
        });

        HBox.setHgrow(pwd, Priority.ALWAYS);
        HBox.setHgrow(copy, Priority.NEVER);
        HBox.setHgrow(copy, Priority.NEVER);
        getChildren().addAll(pwd, copy, fill);
    }
}
