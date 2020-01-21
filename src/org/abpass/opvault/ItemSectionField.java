package org.abpass.opvault;

import org.json.zero.hl.JsonTypedHandler;

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
        handler.objectProperty("a", A.newParser(), (t, o) -> t.a = o);
        
        handler.sectionFieldProperty("v", (t, o) -> {
            System.out.println("Kind when value: " + t.kind);
            t.value = o;
        });
        
        handler.valueProperty("inputTraits", (t, o) -> System.out.println("inputTraits: " + o));
        
        return handler;
    }
    
    private Kind kind;
    private String name;
    private String title;
    private Object value;
    
    private A a;
    
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
    
    static class A {
        static JsonTypedHandler<A> newParser() {
            Json<A> handler = new Json<A>(A::new);
            handler.stringProperty("guarded", (t, o) -> t.guarded = "yes".equalsIgnoreCase(o));
            handler.stringProperty("generate", (t, o) -> t.noGenerate = "off".equalsIgnoreCase(o));
            handler.stringProperty("clipboardFilter", (t, o) -> t.clipboardFilder = o);
            
            // TODO
            handler.valueProperty("multiline", (t, o) -> System.out.println("multiline: " + o));
            
            return handler;
        }
        
        private Boolean guarded;
        private Boolean noGenerate;
        private String clipboardFilder;
    }
    
}
