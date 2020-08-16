package tools;

import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Callback;

public class PrimaryController {
    enum Order {
        FIRST_LAST,
        LAST_FIRST
    }

    @FXML
    private ToggleGroup order;

    @FXML
    private ToggleButton firstLast;

    @FXML
    private ToggleButton lastFirst;

    @FXML
    public BorderPane borderPane;

    @FXML
    private ListView<NavigationData> navigationList;

    @FXML
    private ComboBox<String> classChoices;

    @FXML
    protected void initialize() {
        navigationList.setCellFactory(new Callback<ListView<NavigationData>, ListCell<NavigationData>>() {
            @Override
            public ListCell<NavigationData> call(ListView<NavigationData> param) {
                return new NavigationCell();
            }
        });

        navigationList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            switch (newValue.getName().toUpperCase()) {
                case "CLASSES":
                    borderPane.setCenter(new Classes(this));
                    break;
                case "PICKER":
                    borderPane.setCenter(new Picker(this));
                    break;
                case "GROUPER":
                    borderPane.setCenter(new Group(this));
                    break;
                case "ATTENDANCE":
                    borderPane.setCenter(new Attendance(this));
                    break;
            }
        });

        order.selectedToggleProperty().addListener((obsVal, oldVal, newVal) -> {
            if (newVal == null) {
                oldVal.setSelected(true);
            }
        });
        navigationList.getSelectionModel().select(0);

        for (var name : Classes.CLASS_INFO.keySet()) {
            addClass(name);
        }
    }

    @FXML
    public void displayHelp(ActionEvent actionEvent) {
       HelpWindow help = new HelpWindow();
       help.display(App.STAGE_STACK.peek());
    }

    public void addClass(String name) {
        classChoices.getItems().add(name);

        if (classChoices.getSelectionModel().isEmpty()) {
            classChoices.getSelectionModel().select(0);
        }
    }

    public void setDisplay(Node node) {
        borderPane.setCenter(node);
    }

    public void listenToClassChange(ChangeListener<? super String> listener) {
        classChoices.valueProperty().addListener(listener);
    }

    public void listenToOrderChange(ChangeListener<? super Toggle> listener) {
        order.selectedToggleProperty().addListener(listener);
    }

    public Order getOrder() {
        final var ordering = order.getSelectedToggle();

        if (ordering == firstLast) {
            return Order.FIRST_LAST;
        }
        else {
            return Order.LAST_FIRST;
        }
    }

    public void removeClass(String name) {
        classChoices.getItems().remove(name);
    }

    public String getActiveClass() {
        return classChoices.getSelectionModel().getSelectedItem();
    }
}
