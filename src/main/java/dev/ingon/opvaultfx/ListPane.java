package dev.ingon.opvaultfx;

import java.util.Comparator;
import java.util.function.Predicate;

import dev.ingon.opvault.Item;
import dev.ingon.opvault.ItemException;
import dev.ingon.opvault.ItemOverview;
import dev.ingon.opvault.Profile;
import dev.ingon.opvault.ProfileException;
import dev.ingon.opvault.Item.Category;
import dev.ingon.opvaultfx.CategoryIcons.Size;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
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
    
    private static final Predicate<ItemWithOverview> ALL_PREDICATE = 
            (i) -> i.item != null && i.item.getCategory() != Category.Tombstone;
    
    private final ListProperty<ItemWithOverview> allItems = 
            new SimpleListProperty<ItemWithOverview>(this, "allItems", FXCollections.observableArrayList());
    
    private final ObjectProperty<ItemWithOverview> item = 
            new SimpleObjectProperty<ItemWithOverview>(this, "selectedItem");
    
    private final Comparator<ItemWithOverview> titleComparator = _NULL_SAFE_ITEM;
    
    private final SortedList<ItemWithOverview> sortedItems = 
            new SortedList<ItemWithOverview>(allItems, titleComparator);
    private final FilteredList<ItemWithOverview> visibleItems = 
            new FilteredList<ItemWithOverview>(sortedItems, ALL_PREDICATE);
    
    private final StringProperty search = new SimpleStringProperty(this, "search");
    private final ListView<ItemWithOverview> list = new ListView<ItemWithOverview>(visibleItems);
    
    public ListPane() {
        setId("list");
        
        list.setId("list-items");
        list.setCellFactory((view) -> new ItemListCell());
        list.setPlaceholder(new Label("Nothing found"));
        setVgrow(list, Priority.ALWAYS);
        getChildren().add(list);
        
        search.addListener((__, ___, value) -> {
            if (value.isBlank()) {
                visibleItems.setPredicate(ALL_PREDICATE);
            } else {
                var pattern = value.toLowerCase();
                visibleItems.setPredicate((i) -> {
                    var t = i.overview.getTitle();
                    return t != null && !i.item.isTrashed() && t.toLowerCase().contains(pattern);
                });
            }
            
            if (!visibleItems.isEmpty()) {
                list.getSelectionModel().select(visibleItems.get(0));
            }
        });
        
        list.getSelectionModel().selectedItemProperty().addListener((__, ___, value) -> {
            item.setValue(value);
        });
    }
    
    public void showProfile(Profile profile) throws ProfileException, ItemException {
        this.allItems.clear();

        var items = profile.getItems();
        try (var overviewKeys = profile.overviewKeys()) {
            for (var i : items) {
                var overview = i.getOverview(overviewKeys);
                this.allItems.add(new ItemWithOverview(i, overview));
            }
        }
        
        this.list.getSelectionModel().select(this.visibleItems.get(0));
    }
    
    public void clearProfile() {
        for (var iwo : allItems) {
            iwo.clear();
        }
        this.allItems.clear();
        
        this.item.set(null);
        this.list.getSelectionModel().clearSelection();
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
    
    public static class ItemWithOverview {
        private Item item;
        private ItemOverview overview;
        
        public ItemWithOverview(Item item, ItemOverview overview) {
            this.item = item;
            this.overview = overview;
        }
        
        public Item getItem() {
            return item;
        }
        
        public ItemOverview getOverview() {
            return overview;
        }
        
        public void clear() {
            this.item = null;
            this.overview = null;
        }
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
