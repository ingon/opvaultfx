module abpass {
    requires javafx.controls;
    requires transitive javafx.graphics;

    requires transitive java.sql;
    requires json.simple;
    requires javafx.base;
    
    exports org.abpass.opvault;
    exports org.abpass.ui;
}