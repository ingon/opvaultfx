package org.abpass.opvault;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.json.zero.hl.JsonTypedHandler;

public class ItemDetail {
    static JsonTypedHandler<ItemDetail> newParser() {
        Json<ItemDetail> handler = new Json<ItemDetail>(ItemDetail::new);
        handler.stringProperty("notesPlain", (t, o) -> t.notes = o);
        
        handler.arrayProperty("fields", ItemField.newParser(), (t, o) -> t.fields = o);
        handler.arrayProperty("sections", ItemSection.newParser(), (t, o) -> t.sections = o);
        
        handler.objectProperty("htmlForm", HtmlForm.newParser(), (t, o) -> t.htmlForm = o);
        handler.arrayProperty("passwordHistory", PasswordHistoryItem.newParser(), (t, o) -> t.passwordHistory = o);
        
        return handler;
    }
    
    private String notes;
    
    private List<ItemField> fields = new ArrayList<>();
    private List<ItemSection> sections = new ArrayList<>();
    
    private HtmlForm htmlForm;
    private List<PasswordHistoryItem> passwordHistory;

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
    
    private static class HtmlForm {
        static JsonTypedHandler<HtmlForm> newParser() {
            Json<HtmlForm> handler = new Json<HtmlForm>(HtmlForm::new);
            handler.stringProperty("htmlID", (t, o) -> t.id = o);
            handler.stringProperty("htmlName", (t, o) -> t.name = o);
            handler.stringProperty("htmlMethod", (t, o) -> t.method = o);
            handler.stringProperty("htmlAction", (t, o) -> t.action = o);
            return handler;
        }
        
        private String id;
        private String name;
        private String method;
        private String action;
    }
    
    private static class PasswordHistoryItem {
        static JsonTypedHandler<PasswordHistoryItem> newParser() {
            Json<PasswordHistoryItem> handler = new Json<PasswordHistoryItem>(PasswordHistoryItem::new);
            handler.instantProperty("time", (t, o) -> t.time = o);
            handler.stringProperty("value", (t, o) -> t.value = o);
            return handler;
        }
        
        private Instant time;
        private String value;
    }
}
