package tools;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.function.Function;

public class Group extends VBox {
    static class TableGroup {
        public final ArrayList<String> names = new ArrayList<>();

        public final int groupNumber;

        public TableGroup(int groupNumber) {
            this.groupNumber = groupNumber;
        }
    }

    static class Grouping {
        private final double score;

        private final int[] groups;

        public Grouping() {
            this(Integer.MAX_VALUE, null);
        }

        public Grouping(double score, int[] groups) {
            this.score  = score;
            this.groups = groups;
        }

        public double getScore() {
            return score;
        }

        public int[] getGroups() {
            return groups;
        }
    }

    @FXML
    private Spinner<Integer> studentCount;

    @FXML
    private TableView<TableGroup> groups;

    @FXML
    private Button refresh;

    @FXML
    private RadioButton balanceGroups;

    @FXML
    private ToggleButton present;

    @FXML
    private CheckBox balance;

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
        final int count = controller.getActiveClassroom().size();
        final int maxStudents = count / 2 + count % 2;
        final int studentValue = studentCount.getValue();
        studentCount.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, maxStudents, studentValue));
    }

    @FXML
    private void refreshTable() {
        final var students = getGroups();
        groups.getColumns().clear();
        groups.getItems().clear();
        if (students.isEmpty()) {
            return;
        }
        final int columns = Collections.max(students, Comparator.comparingInt(ArrayList::size)).size();

        final TableColumn<TableGroup, String> groupColumn = new TableColumn<>("Group #");
        groupColumn.setEditable(true);
        groupColumn.setCellValueFactory(cell -> new SimpleStringProperty(" Group " + (cell.getValue().groupNumber + 1)));
        groups.getColumns().add(groupColumn);
        for (int i = 0; i < columns; ++i) {
            final int index = i;
            final TableColumn<TableGroup, String> column = new TableColumn<>("Person " + (i + 1));
            column.setCellValueFactory((cell) -> {
                final var names = cell.getValue().names;
                if (index >= names.size()) {
                    return new SimpleStringProperty();
                }
                else if (controller.getOrder() == PrimaryController.Order.FIRST_LAST) {
                    return new SimpleStringProperty(Utilities.reverseName(names.get(index)));
                }
                else {
                    return new SimpleStringProperty(names.get(index));
                }
            });
            groups.getColumns().add(column);
        }

        for (int row = 0; row < students.size(); ++row) {
            final TableGroup grouping = new TableGroup(row);
            for (var student : students.get(row)) {
                grouping.names.add(student.getLastFirst());
            }
            groups.getItems().add(grouping);
        }
    }

    private ArrayList<Student> getActiveStudents() {
        if (!present.isSelected()) {
            return controller.getActiveClassroom().getAttendanceSheet().getRoster(AttendanceSheet.Filter.COMPLETE);
        }
        else {
            return controller.getActiveClassroom().getAttendanceSheet().getRoster(AttendanceSheet.Filter.PRESENT);
        }
    }

    private ArrayList<ArrayList<Student>> getGroups() {
        final var students = getActiveStudents();
        Collections.shuffle(students);
        final ArrayList<Function<Integer, Grouping>> methods = new ArrayList<>();
        methods.add(this::packDefault);
        if (balance.isSelected()) {
            methods.add(this::packGreater);
            methods.add(this::packLesser);
        }

        Grouping optimal = new Grouping();
        for (var method : methods) {
            final Grouping grouping = method.apply(students.size());
            if (grouping != null && grouping.score < optimal.score) {
                optimal = grouping;
            }
        }

        final ArrayList<ArrayList<Student>> groups = new ArrayList<>();
        int index = 0;
        if (optimal.getGroups() != null) {
            for (var columns : optimal.getGroups()) {
                final ArrayList<Student> row = new ArrayList<>();
                for (int i = 0; i < columns; ++i) {
                    row.add(students.get(index + i));
                }
                groups.add(row);
                index += columns;
            }
        }
        return groups;
    }

    private Grouping packDefault(Integer count) {
        final int target = studentCount.getValue();
        final int groupCount = count / target;
        final int remaining = count % target;
        final double score = (double)target / remaining;
        final int[] groups = new int[groupCount + (remaining > 0 ? 1 : 0)];
        Arrays.fill(groups, 0, groupCount, target);
        Arrays.fill(groups, groupCount, groups.length, remaining);
        return new Grouping(score, groups);
    }

    private Grouping packLesser(Integer count) {
        final int target = studentCount.getValue();
        final int groupCount = count / target;
        final int remaining = count % target;
        if (groupCount == 0) {
            return null;
        }
        final int added = (int)Math.round(groupCount * (target - remaining) / (double)(groupCount + 1));
        final int min   = added % groupCount;
        final int full  = added / groupCount;
        final int slots = target - full;
        final double score = Math.pow(((slots - 1)/(double)slots), min) * Math.pow(slots/(double)target, groupCount) * (remaining + added)/(double)target;
        final int[] groups = new int[groupCount + 1];
        Arrays.fill(groups, 0, groupCount-min, slots);
        Arrays.fill(groups, groupCount-min, groupCount, slots - 1);
        Arrays.fill(groups, groupCount, groups.length, remaining + added);
        return new Grouping(1.0 / score, groups);
    }

    private Grouping packGreater(Integer count) {
        final int target = studentCount.getValue();
        final int groupCount = count / target;
        final int remaining = count % target;
        if (groupCount == 0) {
            return null;
        }
        final int fullPacked = remaining / groupCount;
        final int extra = remaining % groupCount;
        final int filled = target + (int)fullPacked;
        final double ratio = 1.0 + (double)fullPacked / target;
        final double score = Math.pow(ratio, groupCount) * Math.pow((double)(filled + 1)/filled, extra);
        final int[] groups = new int[groupCount];
        Arrays.fill(groups, 0, extra, filled + 1);
        Arrays.fill(groups, extra, groups.length, filled);
        return new Grouping(score * 1.2, groups);
    }
}
