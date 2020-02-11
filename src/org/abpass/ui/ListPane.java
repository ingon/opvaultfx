package org.abpass.ui;

import java.util.Comparator;
import java.util.function.Predicate;

import org.abpass.opvault.Item.Category;
import org.abpass.opvault.ItemException;
import org.abpass.opvault.ItemOverview;
import org.abpass.opvault.Profile;
import org.abpass.opvault.ProfileException;
import org.abpass.ui.CategoryIcons.Size;

import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class ListPane extends VBox {
    private static final Comparator<String> _NULL_SAFE_STRING = 
            Comparator.nullsFirst(String::compareToIgnoreCase);
    private static final Comparator<ItemOverview> _NULL_SAFE_OVERVIEW = 
            Comparator.nullsFirst(Comparator.comparing((o) -> o.getTitle(), _NULL_SAFE_STRING)); 
    private static final Comparator<ItemWithOverview> _NULL_SAFE_ITEM = 
            Comparator.nullsFirst(Comparator.comparing((o) -> o.overview, _NULL_SAFE_OVERVIEW)); 
    
    private static final Predicate<ItemWithOverview> ALL_PREDICATE = (i) -> i.item.getCategory() != Category.Tombstone;
    
    private final ListProperty<ItemWithOverview> allItems = 
            new SimpleListProperty<ItemWithOverview>(this, "allItems", FXCollections.observableArrayList());
    
    private final ObjectProperty<ItemWithOverview> item = 
            new SimpleObjectProperty<ItemWithOverview>(this, "selectedItem");
    
    private final Comparator<ItemWithOverview> titleComparator = _NULL_SAFE_ITEM;
    
    private final SortedList<ItemWithOverview> sortedItems = new SortedList<ItemWithOverview>(allItems, titleComparator);
    private final FilteredList<ItemWithOverview> visibleItems = new FilteredList<ItemWithOverview>(sortedItems, ALL_PREDICATE);
    
    private StringProperty search = new SimpleStringProperty(this, "search");
    private ListView<ItemWithOverview> list;
    
    public ListPane() {
        setId("list");
        
        list = new ListView<ItemWithOverview>(visibleItems);
        list.setId("list-items");
        list.setCellFactory((view) -> new ItemListCell());
        setVgrow(list, Priority.ALWAYS);
        getChildren().add(list);
        
        search.addListener((source, oldValue, newValue) -> {
            if (newValue.isBlank()) {
                visibleItems.setPredicate(ALL_PREDICATE);
            } else {
                visibleItems.setPredicate((i) -> {
                    var t = i.overview.getTitle();
                    return t != null && !i.item.isTrashed() && t.toLowerCase().contains(newValue.toLowerCase());
                });
            }
            if (!visibleItems.isEmpty()) {
                list.getSelectionModel().select(visibleItems.get(0));
            }
        });
        
        list.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<ItemWithOverview>() {
            @Override
            public void changed(ObservableValue<? extends ItemWithOverview> observable, ItemWithOverview oldValue, ItemWithOverview newValue) {
                item.setValue(newValue);
            }
        });
    }
    
    public void showProfile(Profile profile) throws ProfileException, ItemException {
        var items = profile.getItems();
        
        this.allItems.clear();
        try (var overviewKeys = profile.overviewKeys()) {
            for (var i : items) {
                var overview = i.getOverview(overviewKeys);
                this.allItems.add(new ItemWithOverview(i, overview));
            }
        }
        
        this.list.getSelectionModel().select(this.visibleItems.get(0));
    }
    
    public StringProperty searchProperty() {
        return search;
    }
    
    public ItemWithOverview getItem() {
        return this.item.getValue();
    }
    
    public ReadOnlyObjectProperty<ItemWithOverview> itemProperty() {
        return this.item;
    }
    
    private static class ItemListCell extends ListCell<ItemWithOverview> {
        private final GridPane grid = new GridPane();
        private final ImageView icon = new ImageView();
        private final Label title = new Label();
        private final Label detail = new Label();
        
        public ItemListCell() {
            this.getStyleClass().add("item-list-cell");
            grid.getStyleClass().add("item-list-cell-grid");
            icon.getStyleClass().add("item-list-cell-icon");
            title.getStyleClass().add("item-list-cell-title");
            detail.getStyleClass().add("item-list-cell-detail");
            
            grid.add(icon, 0, 0, 1, 2);
            GridPane.setHgrow(icon, Priority.NEVER);
            GridPane.setVgrow(icon, Priority.NEVER);
            GridPane.setHalignment(icon, HPos.CENTER);
            GridPane.setValignment(icon, VPos.TOP);
            
            grid.add(title, 1, 0, 1, 1);
            GridPane.setValignment(title, VPos.TOP);
            
            grid.add(detail, 1, 1, 1, 1);
            GridPane.setValignment(detail, VPos.TOP);
        }
        
        @Override
        protected void updateItem(ItemWithOverview item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                icon.setImage(null);
                grid.maxWidthProperty().unbind();
                setGraphic(null);
                return;
            }
            
            grid.maxWidthProperty().bind(getListView().widthProperty().subtract(44));
            
            icon.setImage(CategoryIcons.get(item.item, Size.SMALL));
            title.setText(item.overview.getTitle());
            detail.setText(item.overview.getAinfo());
            setGraphic(grid);
        }
    }
    
}
