package org.abpass.opvault;

import java.util.ArrayList;
import java.util.List;

import org.abpass.json.JsonArrayHandler;
import org.abpass.json.JsonTypedHandler;

public class ItemDetail {
    static JsonTypedHandler<ItemDetail> newParser() {
        Json<ItemDetail> handler = new Json<ItemDetail>(ItemDetail::new);
        handler.stringProperty("notesPlain", (t, o) -> t.notes = o);
        handler.arrayProperty("fields", new JsonArrayHandler<ItemField>(ItemField.newParser()), (t, o) -> t.fields = o);
        handler.arrayProperty("sections", new JsonArrayHandler<ItemSection>(ItemSection.newParser()), (t, o) -> t.sections = o);
        
        handler.valueProperty("htmlForm", (t, o) -> System.out.println("htmlForm: " + o));
        handler.valueProperty("passwordHistory", (t, o) -> System.out.println("passwordHistory: " + o));
        
        return handler;
    }
    
    String notes;
    List<ItemField> fields = new ArrayList<>();
    List<ItemSection> sections = new ArrayList<>();

    ItemDetail() {
    }
    
    public String getNotes() {
        return notes;
    }

    public List<ItemField> getFields() {
        return fields;
    }
    
    public List<ItemSection> getSections() {
        return sections;
    }
}
