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

    private PrimaryController controller;

    protected static final Map<String, ArrayList<Student>> CLASS_INFO = new LinkedHashMap<>();

    public Classes(PrimaryController controller) {
        this.controller = controller;
        Utilities.loadController(this, "classes.fxml");
    }

    public Classes() {
        this(null);
    }

    @FXML
    public void initialize() {
        classDisplay.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
            @Override
            public ListCell<String> call(ListView<String> param) {
                return new RemovableCell();
            }
        });
        classDisplay.getSelectionModel().selectedItemProperty().addListener((observableValue, old, current) -> {
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
                            CLASS_INFO.put(added.get(i), CLASS_INFO.remove(removed.get(i)));
                            controller.updateClass(removed.get(i), added.get(i));
                        }
                        else {
                            CLASS_INFO.remove(removed.get(i));
                            controller.removeClass(removed.get(i));
                        }
                    }
                }
            }
        });

        controller.listenToOrderChange((observableValue, toggle, t1) -> {
            updateStudents();
        });
        for (var entry : CLASS_INFO.entrySet()) {
            classes.add(entry.getKey());
        }
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

            if (CLASS_INFO.put(name, students) == null) {
                controller.addClass(name);
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
        final var current = classDisplay.getSelectionModel().getSelectedItem();
        if (current == null) {
            students.clear();
            return;
        }
        final var studentInfo = CLASS_INFO.get(current);
        if (studentInfo == null) {
            return;
        }

        students.clear();
        for (var value : studentInfo) {
            if (controller.getOrder() == PrimaryController.Order.FIRST_LAST) {
                students.add(value.getReversed());
            }
            else {
                students.add(value.toString());
            }
        }
        Collections.sort(students, new Comparator<String>() {
            @Override
            public int compare(String lhsName, String rhsName) {
                final String[] lhs = Utilities.extractWords(lhsName);
                final String[] rhs = Utilities.extractWords(rhsName);
                return Arrays.compare(lhs, rhs);
            }
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
        }
    }
}
