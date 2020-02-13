package org.abpass.ui;

import java.nio.file.Paths;

import org.abpass.opvault.ItemException;
import org.abpass.opvault.Profile;
import org.abpass.opvault.ProfileException;
import org.abpass.opvault.ProfileException.InvalidPasswordException;
import org.abpass.opvault.Vault;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class App extends Application {
    private Vault vault;
    private Profile profile;
    
    private StackPane stack;
    private LockedPane lockedPane;
    private UnlockedPane unlockedPane;
    
    @Override
    public void init() throws Exception {
        var freddy = Paths.get("/home/sungon/Downloads/freddy-2013-12-04/onepassword_data");
        var dropbox = Paths.get(System.getProperty("user.home"), "Dropbox", "1Password.opvault");
        
//        vault = new Vault(dropbox);
        vault = new Vault(freddy);
        profile = vault.getDefaultProfile();
    }
    
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
            try {
                profile.unlock(ev.password);
                
                lockedPane.reset(null);
                
                unlockedPane.setProfile(profile);
                stack.getChildren().set(0, unlockedPane);
            } catch (InvalidPasswordException e) {
                lockedPane.reset(e.getMessage());
            } catch (ItemException e) {
                lockedPane.reset(e.getMessage());
            } catch (ProfileException e) {
                lockedPane.reset(e.getMessage());
            }
        });
        
        scene.addEventHandler(ProfileEvent.LOCK, (ev) -> {
            profile.lock();
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
