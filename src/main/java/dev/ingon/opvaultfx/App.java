package dev.ingon.opvaultfx;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class App extends Application {
    private LockedPane lockedPane;
    private UnlockedPane unlockedPane;
    
    @Override
    public void start(Stage primaryStage) throws Exception {
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
        });
        
        primaryStage.setTitle("OPVaultFX");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) throws Exception {
        launch(args);
    }
}
