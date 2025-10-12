module dev.ingon.opvaultfx {
    requires javafx.base;
    requires javafx.controls;
    requires dev.ingon.json.zero;
    requires dev.ingon.otp;
    requires dev.ingon.opvault;
    
    requires transitive javafx.graphics;
    requires transitive java.sql;
    requires java.desktop;
    
    exports dev.ingon.opvaultfx;
}
