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
        handler.stringProperty("k", (t, o) -> {
            t.kind = Kind.of(o);
            addHandlerByKind(handler, t.kind);
        });
        handler.stringProperty("n", (t, o) -> t.name = o);
        handler.stringProperty("t", (t, o) -> t.title = o);
        handler.objectProperty("a", A.newParser(), (t, o) -> t.a = o);
        
        handler.valueProperty("inputTraits", (t, o) -> System.out.println("inputTraits: " + o));
        
        return handler;
    }
    
    private static void addHandlerByKind(Json<ItemSectionField> handler, Kind kind) {
        switch (kind) {
        case Concealed: 
            handler.secureStringProperty("v", ItemSectionField::setValue);
            break;
        case String:
        case Email:
        case URL:
        case Phone:
        case Menu:
        case CC_Type:
            handler.stringProperty("v", ItemSectionField::setValue);
            break;
        case Date:
            handler.instantProperty("v", ItemSectionField::setValue);
            break;
        case Address:
            handler.objectProperty("v", Address.newParser(), ItemSectionField::setValue);
            break;
        case MonthYear:
            handler.monthYearProperty("v", ItemSectionField::setValue);
            break;
        default:
            System.out.println("k : " + kind);
            handler.stringProperty("v", (t, o) -> {
                t.value = o;
                System.out.println("v: " + o);
            });
            break;
        }
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
    
    public A getA() {
        return a;
    }
    
    private void setValue(Object value) {
        this.value = value;
    }
    
    public static class A {
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
        
        public Boolean getGuarded() {
            return guarded;
        }
        public Boolean getNoGenerate() {
            return noGenerate;
        }
        public String getClipboardFilder() {
            return clipboardFilder;
        }
    }
    
    static class Address {
        static JsonTypedHandler<Address> newParser() {
            var handler = new Json<Address>(Address::new);
            
            handler.stringProperty("street", Address::setStreet);
            handler.stringProperty("city", Address::setCity);
            handler.stringProperty("zip", Address::setZip);
            handler.stringProperty("state", Address::setState);
            handler.stringProperty("country", Address::setCountry);
            
            return handler;
        }
        
        private String street;
        private String city;
        private String zip;
        private String state;
        private String country;
        
        public String getStreet() {
            return street;
        }
        
        public String getCity() {
            return city;
        }
        
        public String getZip() {
            return zip;
        }
        
        public String getState() {
            return state;
        }
        
        public String getCountry() {
            return country;
        }
        
        private void setStreet(String street) {
            this.street = street;
        }

        private void setCity(String city) {
            this.city = city;
        }

        private void setZip(String zip) {
            this.zip = zip;
        }

        private void setState(String state) {
            this.state = state;
        }

        private void setCountry(String country) {
            this.country = country;
        }
    }
}
