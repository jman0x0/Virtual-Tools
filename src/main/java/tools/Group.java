package tools;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;

public class Group extends VBox {
    static class Grouping {
        public final ArrayList<String> names = new ArrayList<>();

        public final int groupNumber;

        public Grouping(int groupNumber) {
            this.groupNumber = groupNumber;
        }
    }

    @FXML
    private Spinner<Integer> studentCount;

    @FXML
    private TableView<Grouping> groups;

    @FXML
    private Button refresh;

    @FXML
    private ToggleButton present;

    private final PrimaryController controller;

    public Group(PrimaryController controller) {
        this.controller = controller;
        Utilities.loadController(this, "group.fxml");
        controller.listenToClassChange((observable, old, current) -> {
            refreshTable();
        });
        controller.listenToOrderChange((observableValue, toggle, t1) -> {
            groups.refresh();
        });
    }

    @FXML
    protected void initialize() {
        refreshMaximum();
        groups.setEditable(true);
        studentCount.valueProperty().addListener((observable, old, current) -> {
            refreshTable();
            refreshMaximum();
        });
        present.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
            refreshTable();
        });
    }

    protected void refreshMaximum() {
        final String active = controller.getActiveClass();
        if (active == null) {
            return;
        }
        final var students = new ArrayList<>(Classes.CLASS_INFO.get(active));
        final int count = students.size();
        final int maxStudents = count / 2 + count % 2;
        final int studentValue = studentCount.getValue();
        studentCount.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, maxStudents, studentValue));
    }

    @FXML
    private void refreshTable() {
        final String active = controller.getActiveClass();
        if (active == null) {
            return;
        }
        final var students = getActiveStudents();
        Collections.shuffle(students);
        groups.getColumns().clear();
        groups.getItems().clear();

        final int groupSize = studentCount.getValue();

        final TableColumn<Grouping, String> groupColumn = new TableColumn<>("Group #");
        groupColumn.setEditable(true);
        groupColumn.setCellValueFactory(cell -> new SimpleStringProperty(" Group " + cell.getValue().groupNumber));
        groups.getColumns().add(groupColumn);
        for (int i = 0; i < groupSize; ++i) {
            final int index = i;
            final TableColumn<Grouping, String> column = new TableColumn<>("P" + i);
            column.setCellValueFactory((cell) -> {
                final var names = cell.getValue().names;
                if (index >= names.size()) {
                    return new SimpleStringProperty();
                }
                else if (controller.getOrder() == PrimaryController.Order.FIRST_LAST) {
                    return new SimpleStringProperty(names.get(index));
                }
                else {
                    return new SimpleStringProperty(Utilities.reverseName(names.get(index)));
                }
            });
            groups.getColumns().add(column);
        }

        final int studentCount = students.size();
        final int extra = studentCount % groupSize == 0 ? 0 : 1;
        final int rows = studentCount / groupSize + extra;
        for (int row = 0; row < rows; ++row) {
            final int first = row * groupSize;
            final int last = Math.min(students.size(), first + groupSize);
            final Grouping grouping = new Grouping(row);
            for (int i = first; i < last; ++i) {
                grouping.names.add(students.get(i).toString());
            }
            groups.getItems().add(grouping);
        }
    }

    private ArrayList<Student> getActiveStudents() {
        if (!present.isSelected()) {
            return new ArrayList<>(Classes.CLASS_INFO.get(controller.getActiveClass()));
        }
        else {
            return Attendance.getRoster(controller, Attendance.Filter.PRESENT);
        }
    }
}
