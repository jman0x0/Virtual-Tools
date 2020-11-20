module tools {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires jdk.crypto.ec;

    opens tools to javafx.fxml;
    exports tools;
}