package tools;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Classes extends BorderPane {
    @FXML
    private ListView<String> classDisplay;

    @FXML
    private ListView<String> studentDisplay;

    @FXML
    private ObservableList<String> students;

    @FXML
    private ObservableList<String> classes;

    @FXML
    private Button addStudent;

    private final PrimaryController controller;

    private Classroom classroom;

    public Classes(PrimaryController controller) {
        this.controller = controller;
        Utilities.loadController(this, "classes.fxml");
    }

    @FXML
    public void initialize() {
        classDisplay.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
            @Override
            public ListCell<String> call(ListView<String> param) {
                return new RemovableCell();
            }
        });
        updateStudents();
        classDisplay.getSelectionModel().selectedItemProperty().addListener((observableValue, old, current) -> {
            classroom = controller.getClassroom(current);
            updateStudents();
        });
        controller.listenToOrderChange((observableValue, toggle, t1) -> {
            updateStudents();
        });
        classDisplay.getItems().addListener(new ListChangeListener<String>() {
            @Override
            public void onChanged(Change<? extends String> change) {
                while (change.next()) {
                    final var removed = change.getRemoved();
                    final var added = change.getAddedSubList();
                    for (int i = 0; i < removed.size(); ++i) {
                        if (change.wasReplaced()) {
                            controller.updateClass(removed.get(i), added.get(i));
                        }
                        else {
                            controller.removeClass(removed.get(i));
                        }
                    }
                }
            }
        });
        classes.addAll(controller.getClassSet());
        if (!classes.isEmpty() && classDisplay.getSelectionModel().getSelectedItem() == null) {
            classDisplay.getSelectionModel().select(0);
        }
    }

    public void loadRoster(File file) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            final String name = reader.readLine();
            final ArrayList<Student> students = new ArrayList<>();

            while (reader.ready()) {
                final String line = reader.readLine().trim();
                try {
                    students.add(new Student(line));
                }
                catch (IllegalArgumentException iae) {
                    //iae.printStackTrace();
                }
            }
            if (controller.addClass(name) == null) {
                controller.getClassroom(name).setStudents(students);
                classes.add(name);
            }
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
        finally {
            if (!classes.isEmpty() && classDisplay.getSelectionModel().getSelectedItem() == null) {
                classDisplay.getSelectionModel().select(0);
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }
    }

    public void updateStudents() {
        students.clear();
        if (classroom == null) {
            return;
        }
        for (var value : classroom) {
            if (controller.getOrder() == PrimaryController.Order.FIRST_LAST) {
                students.add(value.getReversed());
            }
            else {
                students.add(value.toString());
            }
        }
        students.sort((String lhsName, String rhsName) -> {
            final String[] lhs = Utilities.extractWords(lhsName);
            final String[] rhs = Utilities.extractWords(rhsName);
            return Arrays.compare(lhs, rhs);
        });
    }

    @FXML
    public void addClasses() {
        final LoadFiles loader = new LoadFiles(true);
        loader.display(App.STAGE_STACK.peek());
        final File[] chosen = loader.getFiles();
        if (chosen != null) {
            for (File file : chosen) {
                loadRoster(file);
            }
            updateStudents();
        }
    }
}
