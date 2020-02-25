module dev.ingon.opvaultfx {
    requires javafx.base;
    requires javafx.controls;
    
    requires transitive javafx.graphics;
    requires transitive java.sql;
    
    requires dev.ingon.json.zero;
    
    exports dev.ingon.opvault;
    exports dev.ingon.opvaultfx;
}