package org.abpass.opvault;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ItemDetail {
    public final Map<String, Object> data;
    
    public ItemDetail(Map<String, Object> data) {
        this.data = data;
    }
    
    public List<ItemField> getFields() {
        var raw = (List<Object>) data.get("fields");
        if (raw == null) {
            return Collections.emptyList();
        }
        return raw.stream()
                .map((o) -> (Map<String, Object>)o)
                .map(ItemField::new)
                .collect(Collectors.toList());
    }
    
    public String getNotes() {
        return (String) data.get("notes");
    }

    public List<ItemSection> getSections() {
        var raw = (List<Object>) data.get("sections");
        if (raw == null) {
            return Collections.emptyList();
        }
        return raw.stream()
                .map((o) -> (Map<String, Object>)o)
                .map(ItemSection::new)
                .collect(Collectors.toList());
    }
    
    @Override
    public String toString() {
        return String.format("itemdetail [ notes=%s ]", getNotes());
    }
}
