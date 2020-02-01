package org.abpass.opvault;

public class ItemField {
    public enum Type {
        Password("P"),
        Text("T"),
        Email("E"),
        Number("N"),
        Radion("R"),
        Telephone("TEL"),
        Checkbox("C"),
        URL("U"),
        B("B"),
        I("I"),
        S("S"),
        EMPTY(""),
        ;
        
        private final String raw;
        
        private Type(String raw) {
            this.raw = raw;
        }
        
        public static Type of(String s) {
            for (var e : Type.values()) {
                if (e.raw.equals(s)) {
                    return e;
                }
            }
            throw new IllegalArgumentException("unknown type: " + s);
        }
    }

    public enum Designation {
        None(""),
        Username("username"),
        Password("password"),
        ;
        
        private final String raw;
        
        private Designation(String raw) {
            this.raw = raw;
        }
        
        public static Designation of(String s) {
            if (s == null) {
                return Designation.None;
            }
            
            for (var e : Designation.values()) {
                if (e.raw.equals(s)) {
                    return e;
                }
            }
            throw new IllegalArgumentException("unknown designation: " + s);
        }
    }
    
    static Json<ItemField> newParser() {
        var handler = new Json<ItemField>(ItemField::new);
        
        handler.stringProperty("id", (t, o) -> t.id = o);
        handler.stringProperty("type", (t, o) -> t.type = Type.of(o));
        handler.stringProperty("name", (t, o) -> t.name = o);
        handler.secureStringProperty("value", (t, o) -> t.value = o);
        handler.stringProperty("designation", (t, o) -> t.designation = Designation.of(o));
        
        return handler;
    }

    private String id;
    private Type type;
    private String name;
    private SecureString value;
    private Designation designation;
    
    ItemField() {
    }
    
    public String getId() {
        return id;
    }

    public Type getType() {
        return type;
    }
    
    public String getName() {
        return name;
    }

    public SecureString getValue() {
        return value;
    }

    public Designation getDesignation() {
        return designation;
    }
}
