module dev.ingon.opvaultfx {
    requires javafx.base;
    requires javafx.controls;
    requires dev.ingon.json.zero;
    
    requires transitive javafx.graphics;
    requires transitive java.sql;
    
    exports dev.ingon.opvault;
    exports dev.ingon.opvaultfx;
}