package org.abpass.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class App extends Application {
    private StackPane stack;
    private LockedPane lockedPane;
    private UnlockedPane unlockedPane;
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        stack = new StackPane();
        stack.setMinSize(400, 300);
        
        lockedPane = new LockedPane();
        unlockedPane = new UnlockedPane();
        
        var scene = new Scene(stack, 1024, 768);
        scene.fillProperty().set(Color.web("#121212"));
        scene.getStylesheets().add("app.css");
        scene.setOnKeyPressed((ev) -> {
           if (ev.getCode() == KeyCode.R && ev.isControlDown() && ev.isShiftDown()) {
               scene.getStylesheets().remove("app.css");
               scene.getStylesheets().add("app.css");
           }
        });
        
        stack.setPrefSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        stack.getChildren().add(lockedPane);
        
        scene.addEventHandler(ProfileEvent.UNLOCK, (ev) -> {
            unlockedPane.showProfile(ev.profile);
            stack.getChildren().set(0, unlockedPane);
        });
        
        scene.addEventHandler(ProfileEvent.LOCK, (ev) -> {
            stack.getChildren().set(0, lockedPane);
            unlockedPane.clearProfile();
        });
        
        primaryStage.setTitle("abpass");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) throws Exception {
        launch(args);
    }
}
