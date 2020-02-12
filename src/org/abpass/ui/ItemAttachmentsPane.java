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
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
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
                        var attPane = new HBox();
                        attPane.getStyleClass().add("item-attachment-row");
                        
                        var img = new ImageView(new Image(new ByteArrayInputStream(att.getIcon())));
                        img.getStyleClass().add("item-attachment-icon");
                        img.setFitHeight(48);
                        img.setPreserveRatio(true);
                        
                        var txt = new TextField(att.getOverview().getFilename());
                        txt.getStyleClass().add("item-attachment-text");
                        
                        var btn = new Button("Download");
                        btn.getStyleClass().add("item-attachment-btn");
                        
                        HBox.setHgrow(img, Priority.NEVER);
                        HBox.setHgrow(txt, Priority.ALWAYS);
                        HBox.setHgrow(btn, Priority.NEVER);
                        attPane.getChildren().addAll(img, txt, btn);
                        
                        getChildren().add(attPane);
                    } catch (ProfileLockedException | ItemAttachmentException e) {
                        e.printStackTrace();
                    }
                }
            }});
    }
}
