package org.abpass.opvault;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ItemSection {
    public final Map<String, Object> data;
    
    public ItemSection(Map<String, Object> data) {
        this.data = data;
    }
    
    public String getName() {
        return (String) data.get("name");
    }

    public String getTitle() {
        return (String) data.get("title");
    }

    public List<ItemSectionField> getFields() {
        var raw = (List<Object>) data.get("fields");
        if (raw == null) {
            return Collections.emptyList();
        }
        return raw.stream()
                .map((o) -> (Map<String, Object>)o)
                .map(ItemSectionField::new)
                .collect(Collectors.toList());
    }
    
    @Override
    public String toString() {
        return String.format("section [ name=%s, title=%s ]", getName(), getTitle());
    }
}
