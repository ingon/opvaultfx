package org.abpass.opvault;

import java.util.Map;

public class ItemSectionField {
    public enum Kind {
        Concealed("concealed"),
        Address("address"),
        Date("date"),
        MonthYear("monthYear"),
        String("string"),
        URL("URL"),
        CC_Type("cctype"),
        Phone("phone"),
        Gender("gender"),
        Email("email"),
        Menu("menu"),
        ;
        
        private final String raw;
        
        private Kind(String raw) {
            this.raw = raw;
        }
        
        public static Kind of(String s) {
            for (var e : Kind.values()) {
                if (e.raw.equals(s)) {
                    return e;
                }
            }
            throw new IllegalArgumentException("unknown kind: " + s);
        }
    }
    
    public final Map<String, Object> data;
    
    public ItemSectionField(Map<String, Object> data) {
        this.data = data;
    }
    
    public Kind getKind() {
        var kind = (String) data.get("k");
        return Kind.of(kind);
    }
    
    public String getName() {
        return (String) data.get("n");
    }

    public String getTitle() {
        return (String) data.get("t");
    }

    public Object getValue() {
        return data.get("v");
    }
    
    @Override
    public String toString() {
        return String.format("sectionField [ kind=%s, name=%s, title=%s, value=%s ]", 
            getKind(), getName(), getTitle(), getValue());
    }
}
