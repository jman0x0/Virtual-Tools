package tools;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Classes extends BorderPane implements Refreshable {
    @FXML
    private ListView<String> classDisplay;

    @FXML
    private ListView<String> studentDisplay;

    @FXML
    private ObservableList<String> students;

    @FXML
    private ObservableList<String> classes;

    private ListChangeListener<String> classListener = new ListChangeListener<>() {
        @Override
        public void onChanged(Change<? extends String> change) {
            while (change.next()) {
                final var removed = change.getRemoved();
                final var added = change.getAddedSubList();
                for (int i = 0; i < removed.size(); ++i) {
                    if (change.wasReplaced()) {
                        controller.updateClass(removed.get(i), added.get(i));
                    } else {
                        controller.removeClass(removed.get(i));
                    }
                }
            }
        }
    };

    private ListChangeListener<String> studentListener = new ListChangeListener<String>() {
        @Override
        public void onChanged(Change<? extends String> change) {
            if (classDisplay.getSelectionModel().isEmpty()) {
                return;
            }
            final String classname = classDisplay.getSelectionModel().getSelectedItem();
            final Classroom classroom = controller.getClassroom(classname);
            while (change.next()) {
                final var removed = change.getRemoved();

                for (int i = 0; i < removed.size(); ++i) {
                    classroom.remove(classroom.findStudent(removed.get(i)));
                }
            }
        }
    };

    private final PrimaryController controller;

    private Classroom classroom;

    public Classes(PrimaryController controller) {
        this.controller = controller;
        Utilities.loadController(this, "classes.fxml");
    }

    @FXML
    public void initialize() {
        classDisplay.setCellFactory(factory -> new RemovableCell());
        studentDisplay.setCellFactory(factory -> new RemovableCell());
        updateStudents();
        classDisplay.getSelectionModel().selectedItemProperty().addListener((observableValue, old, current) -> {
            classroom = controller.getClassroom(current);
            updateStudents();
        });

        controller.listenToOrderChange((observableValue, toggle, t1) -> {
            updateStudents();
        });
        classes.addListener(classListener);
        students.addListener(studentListener);
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
    public void moveUp() {
        move(-1);
    }

    @FXML
    public void moveDown() {
        move(1);
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

    private void move(int displacement) {
        final int selection = classDisplay.getSelectionModel().getSelectedIndex();
        if (selection >= 0 && displacement != 0) {
            final ArrayList<Classroom> saved = new ArrayList<>();
            for (var entry : controller.getClassroomEntries()) {
                saved.add(entry.getValue());
            }
            final int next = Math.floorMod(selection + displacement, classes.size());
            swapIndex(saved, selection, next);
            swapIndex(classes, selection, next);
            controller.clearClassroom();
            for (var classroom : saved) {
                controller.addClass(classroom.getName(), classroom);
            }
            classDisplay.getSelectionModel().select(next);
        }
    }

    private <E> void swapIndex(List<E> list, int first, int last) {
        final var temp = list.get(first);
        list.set(first, list.get(last));
        list.set(last, temp);
    }

    @Override
    public void refresh() {
        final int selected = classDisplay.getSelectionModel().getSelectedIndex();
        final String selection = selected >= 0 ? classes.get(selected) : null;
        classes.removeListener(classListener);
        classes.clear();
        classes.addAll(controller.getClassSet());
        classes.addListener(classListener);
        final var position = classes.indexOf(selection);
        if (position >= 0) {
            classDisplay.getSelectionModel().select(position);
        }
    }

    @FXML
    public void addStudent() {
        if (classDisplay.getSelectionModel().isEmpty()) {
            return;
        }
        final var component = Utilities.<Region, AddStudent>loadFXML("add_student.fxml");
        component.getValue().display(App.STAGE_STACK.peek());
        final Student created = component.getValue().getStudent();
        if (created != null) {
            final String item = classDisplay.getSelectionModel().getSelectedItem();
            controller.getClassroom(item).add(created);
            updateStudents();
        }
    }
}
