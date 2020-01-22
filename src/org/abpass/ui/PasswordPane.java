package org.abpass.ui;

import org.abpass.opvault.SecureString;

import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.BorderPane;

public class PasswordPane extends BorderPane {
    public PasswordPane(SecureString data) {
        var pwd = new PasswordField();
        pwd.setText("use the copy button");
        pwd.setEditable(false);
        pwd.setDisable(true);
        setCenter(pwd);

        var btn = new Button("Copy");
        btn.setOnAction((e) -> {});
        setRight(btn);
    }
}
