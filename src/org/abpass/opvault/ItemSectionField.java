package org.abpass.opvault;

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
    
    static Json<ItemSectionField> newParser() {
        var handler = new Json<ItemSectionField>(ItemSectionField::new);
        handler.stringProperty("k", (t, o) -> t.kind = Kind.of(o));
        handler.stringProperty("n", (t, o) -> t.name = o);
        handler.stringProperty("t", (t, o) -> t.title = o);
        handler.primitiveProperty("v", (t, o) -> t.value = o != null ? o.toString() : null);
        
        handler.valueProperty("a", (t, o) -> System.out.println("SF [a]: " + o)); // {guarded=yes, generate=off, clipboardFilter=0123456789}?
        handler.valueProperty("zip", (t, o) -> System.out.println("SF [zip]: " + o)); // ?
        handler.valueProperty("state", (t, o) -> System.out.println("SF [state]: " + o)); // ?
        handler.valueProperty("country", (t, o) -> System.out.println("SF [country]: " + o)); // ?
        handler.valueProperty("street", (t, o) -> System.out.println("SF [country]: " + o)); // ?
        
        return handler;
    }
    
    Kind kind;
    String name;
    String title;
    String value;
    
    ItemSectionField() {
    }
    
    public Kind getKind() {
        return kind;
    }
    
    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public Object getValue() {
        return value;
    }
}
