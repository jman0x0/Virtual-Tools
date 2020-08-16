package tools;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.layout.VBox;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class Attendance extends VBox {
    @FXML
    private ObservableList<String> names;

    @FXML
    private ListView<String> display;

    @FXML
    private Button loadBox;

    @FXML
    private ToggleGroup filter;

    @FXML
    private ToggleButton present;

    @FXML
    private ToggleButton absent;

    private static final ArrayList<String> NAME_LIST = new ArrayList<>();

    private PrimaryController controller;

    @FXML
    protected void initialize() {
        Draggable.initialize(loadBox);
        loadBox.setOnDragDropped(this::dragDropped);

        filter.selectedToggleProperty().addListener((obsVal, oldVal, newVal) -> {
            if (newVal == null) {
                oldVal.setSelected(true);
            }
            else {
                updateNames();
            }
        });

        if (!NAME_LIST.isEmpty()) {
            updateNames();
        }
    }

    public Attendance(PrimaryController controller) {
        this.controller = controller;
        Utilities.loadController(this, "attendance.fxml");
        this.controller.listenToClassChange((observableValue, old, current) -> {
            updateNames();
        });
        this.controller.listenToOrderChange((observableValue, old, current) -> {
            updateOrder();
        });
    }

    public void loadAttendanceSheet(File file) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            NAME_LIST.clear();

            while (reader.ready()) {
                final String line = reader.readLine();
                final String name = line.substring(0, line.indexOf(','));
                if (name.contains("(")) {
                    continue;
                }
                NAME_LIST.add(name);
            }
            updateNames();
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
        finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }
    }

    public void updateNames() {
        clearDisplay();
        final String className = controller.getActiveClass();
        if (className == null) {
            return;
        }
        final boolean ABSENT_MODE = filter.getSelectedToggle() == absent;
        final var roster = getRoster(controller, !ABSENT_MODE);
        for (var student : roster) {
            if (controller.getOrder() == PrimaryController.Order.FIRST_LAST) {
                names.add(student.getReversed());
            }
            else {
                names.add(student.toString());
            }
        }
    }

    public static ArrayList<Student> getRoster(PrimaryController controller, boolean present) {
        final String className = controller.getActiveClass();
        if (className == null) {
            return null;
        }
        final var roster = Classes.CLASS_INFO.get(className);
        final var marked = getAppropriateMarker(className, present);
        for (String fullName : NAME_LIST) {
            final String[] split = Utilities.extractWords(fullName);
            processName(split, roster, marked, !present);
        }
        return marked;
    }

    public static void processName(String[] split, ArrayList<Student> roster, ArrayList<Student> marked, boolean ABSENT_MODE) {
        final String[] reversed = Arrays.copyOf(split, split.length);
        Utilities.reverseArray(reversed);

        for (var student : roster) {
            if (matchStudent(split, student) || matchStudent(reversed, student)) {
                if (ABSENT_MODE) {
                    marked.remove(student);
                }
                else {
                    marked.add(student);
                }
                return;
            }
        }
    }

    private static ArrayList<Student> getAppropriateMarker(String className, boolean present) {
        if (!present) {
            return new ArrayList<>(Classes.CLASS_INFO.get(className));
        }
        else {
            return new ArrayList<>();
        }
    }

    public static boolean matchStudent(String[] split, Student student) {
        int active = 0;
        for (; active < split.length; ++active) {
            if (!student.firstMatches(split[active])) {
                break;
            }
        }
        if (active >= split.length) {
            return false;
        }
        while (active < split.length) {
            if (!student.lastMatches(split[active++])) {
                return false;
            }
        }
        return true;
    }

    public void clearDisplay() {
        names.clear();
    }

    private void dragDropped(DragEvent drag) {
        final Dragboard dragboard = drag.getDragboard();
        final boolean success = dragboard.hasFiles();
        if (success) {
            for (File file : dragboard.getFiles()) {
                loadAttendanceSheet(file);
            }
        }
        drag.setDropCompleted(success);
        drag.consume();
    }

    private void updateOrder() {
        for (int i = 0; i < names.size(); ++i) {
            names.set(i, Utilities.reverseName(names.get(i)));
        }
    }
}
