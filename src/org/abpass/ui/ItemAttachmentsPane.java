package org.abpass.ui;

import java.io.ByteArrayInputStream;

import org.abpass.opvault.ItemAttachment;
import org.abpass.opvault.ItemAttachmentException;
import org.abpass.opvault.ProfileException.ProfileLockedException;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableListValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class ItemAttachmentsPane extends VBox {
    public ItemAttachmentsPane(ObservableListValue<ItemAttachment> attachments) {
        getStyleClass().add("item-attachments");
        
        attachments.addListener(new ChangeListener<ObservableList<ItemAttachment>>() {
            @Override
            public void changed(ObservableValue<? extends ObservableList<ItemAttachment>> observable,
                    ObservableList<ItemAttachment> oldValue, ObservableList<ItemAttachment> newValue) {
                getChildren().clear();
                
                for (var att : newValue) {
                    try {
                        var attPane = new BorderPane();
                        
                        var img = new ImageView(new Image(new ByteArrayInputStream(att.getIcon())));
                        img.setFitHeight(40);
                        img.setPreserveRatio(true);
                        
                        attPane.setLeft(img);
                        attPane.setCenter(new Label(att.getOverview().getFilename()));
                        attPane.setRight(new Button("Download"));
                        
                        getChildren().add(attPane);
                    } catch (ProfileLockedException | ItemAttachmentException e) {
                        e.printStackTrace();
                    }
                }
            }});
    }
}
