module tools {
    requires javafx.controls;
    requires javafx.fxml;

    opens tools to javafx.fxml;
    exports tools;
}