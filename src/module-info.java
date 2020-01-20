module abpass {
    requires javafx.controls;
    requires transitive javafx.graphics;

    requires transitive java.sql;
    requires javafx.base;
    requires json.zero;
    
    exports org.abpass.opvault;
    exports org.abpass.ui;
}