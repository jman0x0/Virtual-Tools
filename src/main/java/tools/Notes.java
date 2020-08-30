package tools;

import javafx.beans.Observable;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.stream.IntStream;


public class Notes extends VBox implements Refreshable {
    @FXML
    private ObservableList<Student> students;

    @FXML
    private TableView<Student> studentList;

    @FXML
    private TextArea noteArea;

    @FXML
    private ToggleGroup filter;

    @FXML
    private DatePicker dateBegin;

    @FXML
    private DatePicker dateEnd;

    @FXML
    private ToggleButton nonEmpty;

    private final PrimaryController controller;

    public Notes(PrimaryController controller) {
        this.controller = controller;
        Utilities.loadController(this, "notes.fxml");
    }

    @FXML
    private void initialize() {
        studentList.getStyleClass().add("minimalTable");
        filter.selectedToggleProperty().addListener(observable -> updateStudents());
        controller.listenToClassChange((observable, old, current) -> updateStudents());
        controller.listenToOrderChange((observable, old, current) -> updateStudents());
        updateStudents();

        final TableColumn<Student, Integer> tally = new TableColumn<>();
        tally.setCellValueFactory((cell) -> {
            return new SimpleIntegerProperty(getCredit(cell.getValue())).asObject();
        });
        tally.setPrefWidth(48);
        tally.setCellFactory(new Callback<>() {
            @Override
            public TableCell<Student, Integer> call(TableColumn<Student, Integer> param) {
                return new TableCell<>() {
                    @Override
                    public void updateIndex(int i) {
                        super.updateIndex(i);
                        if (getItem() == null) {
                            return;
                        }
                        if (getItem() > 0) {
                            super.setStyle("-fx-text-fill: green");
                        }
                        else if (getItem() == 0) {
                            super.setStyle("-fx-text-fill: black");
                        }
                        else {
                            super.setStyle("-fx-text-fill: red");
                        }
                    }

                    @Override
                    protected void updateItem(Integer item, boolean empty) {
                        super.updateItem(item, empty);

                        if (empty || item == null) {
                            setText(null);
                        }
                        else {
                            setText(item.toString());
                        }
                    }
                };
            }
        });
        final TableColumn<Student, String> name = new TableColumn<>();
        name.setCellValueFactory((cell) -> {
            if (controller.getOrder() == PrimaryController.Order.FIRST_LAST) {
                return new SimpleStringProperty(cell.getValue().getFirstLast());
            }
            else {
                return new SimpleStringProperty(cell.getValue().getLastFirst());
            }
        });
        name.setPrefWidth(300);
        studentList.setColumnResizePolicy((parem) -> true);
        studentList.getColumns().add(tally);
        studentList.getColumns().add(name);
        studentList.getSelectionModel().selectedItemProperty().addListener(this::studentSelected);
        noteArea.textProperty().addListener(this::noteChanged);
        noteArea.setDisable(true);
        dateBegin.setValue(LocalDate.now());
        dateEnd.setValue(LocalDate.now());
        dateBegin.valueProperty().addListener((observableValue, old, date) -> updateStudents());
        dateEnd.valueProperty().addListener((observableValue, old, date) -> updateStudents());
    }

    private void updateStudents() {
        final Classroom classroom = controller.getActiveClassroom();
        students.clear();

        if (classroom != null) {
            final Notebook notebook = classroom.getNotebook();
            final boolean nonempty  = filter.getSelectedToggle() == nonEmpty;
            for (Student student : classroom) {
                if (nonempty && notebook.getNote(student).isEmpty()) {
                    continue;
                }
                students.add(student);
            }
            studentList.refresh();
            Utilities.sortStudents(students, controller.getOrder());
        }
    }

    private void noteChanged(Observable observable, String old, String current) {
        final Classroom classroom = controller.getActiveClassroom();
        if (classroom == null) {
            return;
        }
        final Student student = studentList.getSelectionModel().getSelectedItem();
        if (student != null) {
            classroom.getNotebook().setNote(student, current);
            studentList.refresh();
        }

    }

    private int getCredit(Student student) {
        final Classroom activeSubject = controller.getActiveClassroom();
        if (activeSubject == null) {
            return 0;
        }

        final LocalDate begin = dateBegin.getValue();
        final LocalDate end = dateEnd.getValue();
        final ArrayList<Classroom> classrooms = controller.getClassroomsInRange(begin, end);
        final StringBuilder builder = new StringBuilder();
        for (var classroom : classrooms) {
            if (activeSubject.getName().equals(classroom.getName())) {
                final Student managed = classroom.findStudent(student.getFirstLast());
                builder.append(classroom.getNotebook().getNote(managed)).append('\n');
            }
        }
        final Scanner scanner = new Scanner(builder.toString());
        int total = 0;
        while (scanner.hasNext()) {
            final String line = scanner.nextLine().trim();
            if (line.length() == 0 || (line.charAt(0) != '+' && line.charAt(0) != '-')) {
                continue;
            }
            final var first = IntStream.range(0, line.length())
                    .filter(i -> !Character.isDigit(line.charAt(i)) && line.charAt(i) != '-' && line.charAt(i) != '+')
                    .findFirst();

            final int index = first.orElse(line.length());
            try {
                final int value = Integer.parseInt(line.substring(0, index));
                total += value;
            } catch (NumberFormatException ignored) {

            }
        }
        return total;
    }

    private void studentSelected(Observable observable, Student old, Student current) {
        final Classroom classroom = controller.getActiveClassroom();
        noteArea.setDisable(current == null);
        if (classroom == null || current == null) {
            noteArea.clear();
            return;
        }
        final Notebook notebook = classroom.getNotebook();
        final String note = notebook.getNote(current);
        noteArea.setText(note);
    }

    @Override
    public void refresh() {
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        if (dateEnd.getValue().format(formatter).equals(dateBegin.getValue().format(formatter))) {
            dateBegin.setValue(controller.getActiveDate());
            dateEnd.setValue(controller.getActiveDate());
        }
        updateStudents();
    }
}
