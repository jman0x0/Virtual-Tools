module tools {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;

    opens tools to javafx.fxml;
    exports tools;
}