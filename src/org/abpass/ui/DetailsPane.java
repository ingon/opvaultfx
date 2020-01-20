package org.abpass.ui;

import java.security.GeneralSecurityException;

import org.abpass.opvault.Exceptions.InvalidOpdataException;
import org.abpass.opvault.Item;
import org.abpass.opvault.ItemDetail;
import org.abpass.opvault.ItemField;
import org.abpass.opvault.ItemOverview;
import org.abpass.opvault.ItemSection;
import org.json.zero.ParseException;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableListValue;
import javafx.collections.FXCollections;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class DetailsPane extends VBox {
    
    private final ObjectProperty<Item> item = new SimpleObjectProperty<Item>(this, "item");
    private final ObjectProperty<ItemOverview> overview = new SimpleObjectProperty<ItemOverview>(this, "overview");
    private final ObjectProperty<ItemDetail> detail = new SimpleObjectProperty<ItemDetail>(this, "detail");
    
    private final ObservableListValue<ItemField> fields = new SimpleListProperty<ItemField>(this, "fields", FXCollections.observableArrayList());
    private final ObservableListValue<ItemSection> sections = new SimpleListProperty<ItemSection>(this, "sections", FXCollections.observableArrayList());
    
    private final ImageView icon = new ImageView();
    private final Label title = new Label();
    private final Label subtitle = new Label();
    
    private final VBox detailBox = new VBox();
    private final ItemFieldsPane fieldsPane;
    private final ItemSectionsPane sectionsPane;
    
    public DetailsPane() {
        setId("details");
        
        var headerGrid = new GridPane();
        getChildren().add(headerGrid);
        
        icon.setFitWidth(64);
        icon.setFitHeight(64);
        icon.setPreserveRatio(true);
        
        headerGrid.add(icon, 0, 0, 1, 2);
        headerGrid.add(title, 1, 0, 1, 1);
        headerGrid.add(subtitle, 1, 1, 1, 1);

        detailBox.getStyleClass().add("item-detail-box");
        
        fieldsPane = new ItemFieldsPane(fields);
        detailBox.getChildren().add(fieldsPane);
        
        sectionsPane = new ItemSectionsPane(sections);
        detailBox.getChildren().add(sectionsPane);
        
        var detailScroll = new ScrollPane(detailBox);
        detailScroll.setFitToWidth(true);
        getChildren().add(detailScroll);
    }
    
    public void showItem(Item item, ItemOverview overview) throws InvalidOpdataException, GeneralSecurityException, ParseException {
        this.item.setValue(item);
        this.overview.setValue(overview);
        
        if (item == null) {
            title.setText("");
            detail.setValue(null);;
            fields.clear();
            sections.clear();
            
            detailBox.getChildren().clear();
            
            return;
        }
        
        title.setText(overview.getTitle());
        detail.setValue(item.getDetail());
        fields.setAll(detail.getValue().getFields());
        sections.setAll(detail.getValue().getSections());
        
        detailBox.getChildren().clear();
        if (! fields.isEmpty()) {
            detailBox.getChildren().add(fieldsPane);
        }
        if (! sections.isEmpty()) {
            detailBox.getChildren().add(sectionsPane);
        }
    }
}
