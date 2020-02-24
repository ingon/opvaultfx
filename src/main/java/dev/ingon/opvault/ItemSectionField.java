package dev.ingon.opvault;

import dev.ingon.json.zero.hl.JsonTypedHandler;

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
        handler.objectProperty("inputTraits", InputTraits.newParser(), (t, o) -> t.inputTraits = o);
        
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
        case Gender:
            handler.stringProperty("v", (t, o) -> t.setValue("female".equals(o) ? Gender.FEMALE : Gender.MALE));
            break;
        }
    }
    
    private Kind kind;
    private String name;
    private String title;
    private Object value;
    
    private A a;
    private InputTraits inputTraits;
    
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
    
    public InputTraits getInputTraits() {
        return inputTraits;
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
            handler.stringProperty("multiline", (t, o) -> t.multiline = "yes".equalsIgnoreCase(o));
            
            return handler;
        }
        
        private Boolean guarded;
        private Boolean noGenerate;
        private String clipboardFilder;
        private Boolean multiline;
        
        public Boolean getGuarded() {
            return guarded;
        }
        
        public Boolean getNoGenerate() {
            return noGenerate;
        }
        
        public String getClipboardFilder() {
            return clipboardFilder;
        }
        
        public Boolean getMultiline() {
            return multiline;
        }
    }
    
    public static class Address {
        static JsonTypedHandler<Address> newParser() {
            var handler = new Json<Address>(Address::new);
            
            handler.stringProperty("street", (t, o) -> t.street = o);
            handler.stringProperty("city", (t, o) -> t.city = o);
            handler.stringProperty("zip", (t, o) -> t.zip = o);
            handler.stringProperty("state", (t, o) -> t.state = o);
            handler.stringProperty("country", (t, o) -> t.country = o);
            
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
    }
    
    public static enum Gender {
        MALE,
        FEMALE;
    }
    
    public static class InputTraits {
        public static enum Keyboard {
            Default,
            EmailAddress,
            NamePhonePad,
            NumbersAndPunctuation,
            NumberPad,
        }
        
        public static enum AutoCapitalization {
            Words,
            EmailAddress,
        }
        
        static JsonTypedHandler<InputTraits> newParser() {
            var handler = new Json<InputTraits>(InputTraits::new);
            
            handler.stringProperty("keyboard", (t, o) -> t.keyboard = Keyboard.valueOf(o));
            handler.stringProperty("autocapitalization", (t, o) -> t.autoCapitalization = AutoCapitalization.valueOf(o));
            handler.stringProperty("autocorrection", (t, o) -> t.autoCorrection = "yes".equals(o));
            
            return handler;
        }
        
        private Keyboard keyboard;
        private AutoCapitalization autoCapitalization;
        private Boolean autoCorrection;
        
        public Keyboard getKeyboard() {
            return keyboard;
        }
        
        public AutoCapitalization getAutoCapitalization() {
            return autoCapitalization;
        }
        
        public Boolean getAutoCorrection() {
            return autoCorrection;
        }
    }
}
