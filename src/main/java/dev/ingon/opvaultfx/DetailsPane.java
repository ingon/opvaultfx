package dev.ingon.opvaultfx;

import dev.ingon.opvault.Item;
import dev.ingon.opvault.ItemAttachment;
import dev.ingon.opvault.ItemDetail;
import dev.ingon.opvault.ItemException;
import dev.ingon.opvault.ItemField;
import dev.ingon.opvault.ItemOverview;
import dev.ingon.opvault.ItemSection;
import dev.ingon.opvault.ProfileException.ProfileLockedException;
import dev.ingon.opvaultfx.CategoryIcons.Size;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableListValue;
import javafx.collections.FXCollections;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class DetailsPane extends VBox {
    
    private final ObjectProperty<Item> item = new SimpleObjectProperty<Item>(this, "item");
    private final ObjectProperty<ItemOverview> overview = new SimpleObjectProperty<ItemOverview>(this, "overview");
    private final ObjectProperty<ItemDetail> detail = new SimpleObjectProperty<ItemDetail>(this, "detail");
    
    private final ObservableListValue<ItemField> fields = 
            new SimpleListProperty<ItemField>(this, "fields", FXCollections.observableArrayList());
    private final ObservableListValue<ItemAttachment> attachments = 
            new SimpleListProperty<ItemAttachment>(this, "attachments", FXCollections.observableArrayList());
    private final ObservableListValue<ItemSection> sections = 
            new SimpleListProperty<ItemSection>(this, "sections", FXCollections.observableArrayList());
    
    private final ImageView icon = new ImageView();
    private final Label title = new Label();
    private final Label subtitle = new Label();
    
    private final VBox detailBox = new VBox();
    private final ScrollPane detailScroll = new ScrollPane(detailBox);
    private final ItemPasswordPane passwordPane = new ItemPasswordPane(detail);
    private final ItemFieldsPane fieldsPane = new ItemFieldsPane(fields);
    private final ItemNotesPane notesPane = new ItemNotesPane(detail);
    private final ItemSectionsPane sectionsPane = new ItemSectionsPane(sections);
    private final ItemURLsPane urlsPane = new ItemURLsPane(overview);
    private final ItemAttachmentsPane attachmentsPane = new ItemAttachmentsPane(attachments);
    
    public DetailsPane() {
        setId("details");
        
        var headerGrid = new GridPane();
        headerGrid.setId("details-header");
        getChildren().add(headerGrid);
        
        icon.setId("details-header-icon");
        GridPane.setValignment(icon, VPos.TOP);
        headerGrid.add(icon, 0, 0, 1, 2);
        
        title.setId("details-header-title");
        headerGrid.add(title, 1, 0, 1, 1);
        
        subtitle.setId("details-header-subtitle");
        headerGrid.add(subtitle, 1, 1, 1, 1);

        detailBox.setId("details-data");
        detailBox.getChildren().addAll(passwordPane, fieldsPane, sectionsPane, notesPane, urlsPane, attachmentsPane);
        
        detailScroll.setFitToWidth(true);
        getChildren().add(detailScroll);
    }
    
    public void clearItem() {
        item.setValue(null);
        overview.setValue(null);
        
        icon.setImage(null);
        title.setText("");
        subtitle.setText("");
        detail.setValue(null);
        
        fields.clear();
        sections.clear();
        attachments.clear();
        
        detailBox.getChildren().clear();
    }
    
    public void showItem(Item item, ItemOverview overview) throws ProfileLockedException {
        try {
            this.detail.setValue(item.getDetail());
        } catch (ItemException e) {
            App.showError("Cannot load item detail", e);
            clearItem();
            return;
        }
        
        this.item.setValue(item);
        this.overview.setValue(overview);
        
        icon.setImage(CategoryIcons.get(item, Size.BIG));
        title.setText(overview.getTitle());
        subtitle.setText(item.getCategory().name());
        
        fields.setAll(detail.getValue().getFields());
        sections.setAll(detail.getValue().getSections());
        attachments.setAll(item.getAttachments());
        
        detailBox.getChildren().clear();
        
        if (detail.getValue().getPassword() != null) {
            detailBox.getChildren().add(passwordPane);
        }
        
        if (! fields.isEmpty()) {
            detailBox.getChildren().add(fieldsPane);
        }
        
        if (! sections.isEmpty()) {
            detailBox.getChildren().add(sectionsPane);
        }
        
        if (detail.getValue().getNotes() != null) {
            detailBox.getChildren().add(notesPane);
        }
        
        if (! attachments.isEmpty()) {
            detailBox.getChildren().add(attachmentsPane);
        }
        
        if (overview.getUrl() != null || (overview.getUrls() != null && !overview.getUrls().isEmpty())) {
            detailBox.getChildren().add(urlsPane);
        }
        
        detailScroll.setVvalue(0);
    }
}
