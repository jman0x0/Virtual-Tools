package tools;

import javafx.beans.Observable;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.Callback;


public class Notes extends VBox {
    @FXML
    private ObservableList<Student> students;

    @FXML
    private ListView<Student> studentList;

    @FXML
    private TextArea noteArea;

    @FXML
    private ToggleGroup filter;

    @FXML
    private ToggleButton nonEmpty;

    private final PrimaryController controller;

    public Notes(PrimaryController controller) {
        this.controller = controller;
        Utilities.loadController(this, "notes.fxml");
    }

    @FXML
    private void initialize() {
        filter.selectedToggleProperty().addListener(observable -> updateStudents());
        controller.listenToClassChange((observable, old, current) -> updateStudents());
        controller.listenToOrderChange((observable, old, current) -> updateStudents());
        updateStudents();
        studentList.setCellFactory(new Callback<>() {
            @Override
            public ListCell<Student> call(ListView<Student> list) {
                final ListCell<Student> cell = new ListCell<>() {
                    @Override
                    public void updateItem(Student item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setText(null);
                        }
                        else if (controller.getOrder() == PrimaryController.Order.FIRST_LAST) {
                            setText(item.getFirstLast());
                        }
                        else {
                            setText(item.getLastFirst());
                        }
                    }
                };
                return cell;
            }
        });
        studentList.getSelectionModel().selectedItemProperty().addListener(this::studentSelected);
        noteArea.textProperty().addListener(this::noteChanged);
        noteArea.setDisable(true);
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
        }
    }

    private void studentSelected(Observable observable, Student old, Student current) {
        final Classroom classroom = controller.getActiveClassroom();
        noteArea.setDisable(current == null);
        noteArea.clear();
        if (classroom == null || current == null) {
            return;
        }
        final Notebook notebook = classroom.getNotebook();
        final String note = notebook.getNote(current);
        noteArea.setText(note);
    }
}
