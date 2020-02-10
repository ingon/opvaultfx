package org.abpass.ui;

import java.nio.file.Paths;

import org.abpass.opvault.ItemException;
import org.abpass.opvault.Profile;
import org.abpass.opvault.ProfileException;
import org.abpass.opvault.Vault;
import org.abpass.ui.util.ReloadSceneCssService;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

public class App extends Application {
    private ReloadSceneCssService reloadSvc;

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
        
        reloadSvc = new ReloadSceneCssService();
        reloadSvc.setDelay(Duration.seconds(2));
        reloadSvc.setPeriod(Duration.seconds(1));
        reloadSvc.start();
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        stack = new StackPane();
        stack.setMinSize(400, 300);
        
        lockedPane = new LockedPane(this.vault, this.profile);
        unlockedPane = new UnlockedPane();
        
        var scene = new Scene(stack, 1024, 768);
        scene.fillProperty().set(Color.web("#121212"));
        reloadSvc.addSceneCss(scene, "app.css");
        
        stack.setPrefSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        stack.getChildren().add(lockedPane);
        
        lockedPane.unlocked.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    try {
                        unlockedPane.setProfile(profile);
                        stack.getChildren().set(0, unlockedPane);
                    } catch (ProfileException e) {
                        e.printStackTrace();
                    } catch (ItemException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        
        primaryStage.setTitle("abpass");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) throws Exception {
        launch(args);
    }
}
