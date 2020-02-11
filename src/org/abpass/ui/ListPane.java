package org.abpass.ui;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Comparator;
import java.util.function.Predicate;

import org.abpass.opvault.Item.Category;
import org.abpass.opvault.ItemException;
import org.abpass.opvault.ItemOverview;
import org.abpass.opvault.Profile;
import org.abpass.opvault.ProfileException;

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
import javafx.scene.image.Image;
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
            
            icon.setFitWidth(36);
            icon.setFitHeight(36);
            icon.setPreserveRatio(true);
            
            grid.add(icon, 0, 0, 1, 2);
            GridPane.setHgrow(icon, Priority.NEVER);
            GridPane.setVgrow(icon, Priority.NEVER);
            GridPane.setHalignment(icon, HPos.CENTER);
            GridPane.setValignment(icon, VPos.CENTER);
            
            grid.add(title, 1, 0, 1, 1);
            GridPane.setValignment(title, VPos.CENTER);
            
            grid.add(detail, 1, 1, 1, 1);
            GridPane.setValignment(detail, VPos.CENTER);
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
            
            icon.setImage(getCategoryIcon(item.item.getCategory()));
            title.setText(item.overview.getTitle());
            detail.setText(item.overview.getAinfo());
            setGraphic(grid);
        }
    }
    
    private static Image getCategoryIcon(Category c) {
        switch (c) {
        case CreditCard:
            return getIconFromFile("images/credit_card_white_36dp.png");
        case Identity:
            return getIconFromFile("images/identity_white_36dp.png");
        case Login:
        case Password:
            return getIconFromFile("images/lock_white_36dp.png");
        case SoftwareLicense:
        case Membership:
        case OutdoorLicense:
        case Rewards:
        case SecureNote:
            return getIconFromFile("images/description_white_36dp.png");
        case BankAccount:
            return getIconFromFile("images/account_balance_white_36dp.png");
        case Email:
            return getIconFromFile("images/email_white_36dp.png");
        case Database:
        case Server:
            return getIconFromFile("images/security_white_36dp.png");
        case Router:
            return getIconFromFile("images/router_white_36dp.png");
        case DriverLicense:
        case Passport:
        case SSN:
            return getIconFromFile("images/recent_actors_white_36dp.png");
        default:
            return null;
        }
    }
    
    private static Image getIconFromFile(String name) {
        try {
            return new Image(new FileInputStream(name));
        } catch (FileNotFoundException e) {
            return null;
        }
    }
}
