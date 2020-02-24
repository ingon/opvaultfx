package org.abpass.opvault;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import dev.ingon.json.zero.hl.JsonStringHandler;
import dev.ingon.json.zero.hl.JsonTypedHandler;

public class ItemDetail {
    static JsonTypedHandler<ItemDetail> newParser() {
        Json<ItemDetail> handler = new Json<ItemDetail>(ItemDetail::new);
        handler.stringProperty("notesPlain", (t, o) -> t.notes = o);
        
        handler.arrayProperty("fields", ItemField.newParser(), (t, o) -> t.fields = o);
        handler.arrayProperty("sections", ItemSection.newParser(), (t, o) -> t.sections = o);
        
        handler.secureStringProperty("password", (t, o) -> t.password = o);
        handler.arrayProperty("passwordHistory", PasswordHistoryItem.newParser(), (t, o) -> t.passwordHistory = o);
        handler.arrayProperty("backupKeys", new JsonStringHandler(), (t, o) -> t.backupKeys = o);
        
        handler.objectProperty("htmlForm", HtmlForm.newParser(), (t, o) -> t.htmlForm = o);
        
        return handler;
    }
    
    private String notes;
    
    private List<ItemField> fields = new ArrayList<>();
    private List<ItemSection> sections = new ArrayList<>();
    
    private SecureString password;
    private List<PasswordHistoryItem> passwordHistory;
    private List<String> backupKeys;
    
    private HtmlForm htmlForm;

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
    
    public SecureString getPassword() {
        return password;
    }
    
    public List<PasswordHistoryItem> getPasswordHistory() {
        return passwordHistory;
    }
    
    public List<String> getBackupKeys() {
        return backupKeys;
    }
    
    public HtmlForm getHtmlForm() {
        return htmlForm;
    }
    
    public static class HtmlForm {
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
        
        public String getId() {
            return id;
        }
        
        public String getName() {
            return name;
        }
        
        public String getMethod() {
            return method;
        }
        
        public String getAction() {
            return action;
        }
    }
    
    public static class PasswordHistoryItem {
        static JsonTypedHandler<PasswordHistoryItem> newParser() {
            Json<PasswordHistoryItem> handler = new Json<PasswordHistoryItem>(PasswordHistoryItem::new);
            handler.instantProperty("time", (t, o) -> t.time = o);
            handler.secureStringProperty("value", (t, o) -> t.value = o);
            return handler;
        }
        
        private Instant time;
        private SecureString value;
        
        public Instant getTime() {
            return time;
        }
        
        public SecureString getValue() {
            return value;
        }
    }
}
