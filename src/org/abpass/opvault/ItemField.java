package org.abpass.opvault;

import java.util.Map;

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

    public final Map<String, Object> data;
    
    public ItemField(Map<String, Object> data) {
        this.data = data;
    }

    public Type getType() {
        var s = (String) data.get("type");
        return Type.of(s);
    }
    
    public String getName() {
        return (String) data.get("name");
    }

    public String getValue() {
        return (String) data.get("value");
    }

    public Designation getDesignation() {
        var s = (String) data.get("designation");
        return Designation.of(s);
    }
    
    @Override
    public String toString() {
        return String.format("field [ type=%s, name=%s, value=%s, designation=%s ]", 
            getType(), getName(), getValue(), getDesignation());
    }
}
