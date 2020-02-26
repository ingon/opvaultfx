package dev.ingon.opvaultfx;

import java.io.ByteArrayInputStream;

import dev.ingon.opvault.ItemAttachment;
import dev.ingon.opvault.ItemAttachmentException;
import dev.ingon.opvault.ProfileException.ProfileLockedException;
import javafx.beans.value.ObservableListValue;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class ItemAttachmentsPane extends VBox {
    public ItemAttachmentsPane(ObservableListValue<ItemAttachment> observableAttachments) {
        getStyleClass().add("item-attachments");

        observableAttachments.addListener((__, ___, attachments) -> {
            getChildren().clear();

            for (var att : attachments) {
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
                } catch (ProfileLockedException e) {
                    App.showError("Profile already locked", e);
                    fireEvent(ProfileEvent.lock());
                } catch (ItemAttachmentException e) {
                    App.showError("Cannot load attachment: " + att.getUUID(), e);
                }
            }
        });
    }
}
