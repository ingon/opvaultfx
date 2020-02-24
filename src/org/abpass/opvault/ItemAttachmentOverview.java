package org.abpass.opvault;

import dev.ingon.json.zero.hl.JsonTypedHandler;

public class ItemAttachmentOverview {
    static JsonTypedHandler<ItemAttachmentOverview> newParser() {
        Json<ItemAttachmentOverview> handler = new Json<ItemAttachmentOverview>(ItemAttachmentOverview::new);

        handler.stringProperty("filename", (t, o) -> t.filename = o);
        
        return handler;
    }
    
    private String filename;
    
    public String getFilename() {
        return filename;
    }
}
