package org.abpass.ui;

import org.abpass.opvault.Item;
import org.abpass.opvault.ItemOverview;

public class ItemWithOverview {
    public final Item item;
    public final ItemOverview overview;
    
    public ItemWithOverview(Item item, ItemOverview overview) {
        this.item = item;
        this.overview = overview;
    }
}
