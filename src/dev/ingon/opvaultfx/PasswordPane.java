package dev.ingon.opvaultfx;

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

        var btn = new Button("Copy");
        btn.getStyleClass().add("password-copy");
        btn.setOnAction((e) -> {
            var clipboard = Clipboard.getSystemClipboard();
            var content = new ClipboardContent();
            data.accept((chars) -> {
                content.putString(new String(chars));
                clipboard.setContent(content);
            });
        });

        HBox.setHgrow(pwd, Priority.ALWAYS);
        HBox.setHgrow(btn, Priority.NEVER);
        getChildren().addAll(pwd, btn);
    }
}
