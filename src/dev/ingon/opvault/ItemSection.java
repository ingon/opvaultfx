package dev.ingon.opvault;

import java.util.List;

public class ItemSection {
    static Json<ItemSection> newParser() {
        var handler = new Json<ItemSection>(ItemSection::new);
        handler.stringProperty("name", (t, o) -> t.name = o);
        handler.stringProperty("title", (t, o) -> t.name = o);
        handler.arrayProperty("fields", ItemSectionField.newParser(), (t, o) -> t.fields = o);
        return handler;
    }
    
    private String name;
    private String title;
    private List<ItemSectionField> fields;
    
    ItemSection() {
    }
    
    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public List<ItemSectionField> getFields() {
        return fields;
    }
}
