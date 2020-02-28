package dev.ingon.opvaultfx;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class App extends Application {
    private LockedPane lockedPane;
    private UnlockedPane unlockedPane;
    
    @Override
    public void start(Stage primaryStage) {
        lockedPane = new LockedPane();
        unlockedPane = new UnlockedPane();
        
        var scene = new Scene(lockedPane, 1024, 768);
        scene.fillProperty().set(Color.web("#121212"));
        scene.getStylesheets().add("app.css");
        scene.setOnKeyPressed((ev) -> {
           if (ev.getCode() == KeyCode.R && ev.isControlDown() && ev.isShiftDown()) {
               scene.getStylesheets().remove("app.css");
               scene.getStylesheets().add("app.css");
           }
        });
        
        scene.addEventHandler(ProfileEvent.UNLOCK, (ev) -> {
            unlockedPane.showProfile(ev.profile);
            scene.setRoot(unlockedPane);
        });
        
        scene.addEventHandler(ProfileEvent.LOCK, (ev) -> {
            scene.setRoot(lockedPane);
            unlockedPane.clearProfile();
            lockedPane.show();
        });
        
        primaryStage.setTitle("OPVaultFX");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        lockedPane.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static void showError(String header, Exception exc) {
        showError(header, exc.getLocalizedMessage());
    }
    
    public static void showError(String header, String message) {
        var alert = new Alert(AlertType.ERROR, message);
        alert.setHeaderText(header);
        alert.show();
    }
}
