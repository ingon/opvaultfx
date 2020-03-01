package dev.ingon.opvaultfx;

import java.awt.AWTException;

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
        
        var type = new Button("Type");
        type.getStyleClass().add("password-type");
        type.setOnAction((__) -> {
            try {
                KeyboardRobot robot = new KeyboardRobot();
                robot.focusPreviousApp();
                robot.type(data);
            } catch (AWTException e) {
                App.showError("Cannot auto type", e.getMessage());
            }
        });

        HBox.setHgrow(pwd, Priority.ALWAYS);
        HBox.setHgrow(copy, Priority.NEVER);
        HBox.setHgrow(copy, Priority.NEVER);
        getChildren().addAll(pwd, copy, type);
    }
}
