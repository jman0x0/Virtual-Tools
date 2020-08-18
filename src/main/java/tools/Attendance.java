package tools;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Callback;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Attendance extends VBox {
    public enum Filter {
        COMPLETE,
        ABSENT,
        PRESENT;

        public static Filter fromToggleGroup(ToggleGroup group, ToggleButton complete, ToggleButton present, ToggleButton absent) {
            if (group.getSelectedToggle() == complete) {
                return COMPLETE;
            }
            else if (group.getSelectedToggle() == present) {
                return PRESENT;
            }
            else {
                return ABSENT;
            }
        }
    }

    @FXML
    private ObservableList<Student> students;

    @FXML
    private ListView<Student> display;

    @FXML
    private Button loadBox;

    @FXML
    private TextField loaded;

    @FXML
    private ToggleGroup filter;

    @FXML
    private ToggleButton present;

    @FXML
    private ToggleButton complete;

    @FXML
    private ToggleButton absent;

    private static final Map<String, Map<Student, SimpleBooleanProperty>> CLASS_MAP = new HashMap<>();

    private static final ArrayList<String> NAME_LIST = new ArrayList<>();

    private PrimaryController controller;

    @FXML
    protected void initialize() {
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
        display.setEditable(true);
        updateAttendanceMap();
        final Callback<Student, ObservableValue<Boolean>> itemToBoolean = (Student student) -> {
            final String active = controller.getActiveClass();
            if (active == null) {
                return null;
            }
            return CLASS_MAP.get(active).get(student);
        };
        display.setCellFactory(factory -> new StudentCell(itemToBoolean, controller));
        updateNames();
    }

    public Attendance(PrimaryController controller) {
        this.controller = controller;
        Utilities.loadController(this, "attendance.fxml");
        this.controller.listenToClassChange((observableValue, old, current) -> {
            updateAttendanceMap();
            updateNames();
        });
        this.controller.listenToOrderChange((observableValue, old, current) -> {
            final HashMap<Student, String[]> names = new HashMap<>();
            for (var student : students) {
                if (controller.getOrder() == PrimaryController.Order.FIRST_LAST) {
                    names.put(student, Utilities.extractWords(student.getReversed()));
                }
                else {
                    names.put(student, Utilities.extractWords(student.toString()));
                }
            }
            Collections.sort(students, new Comparator<Student>() {
                @Override
                public int compare(Student lhs, Student rhs) {
                    return Arrays.compare(names.get(lhs), names.get(rhs));
                }
            });
        });
    }

    public void updateAttendanceSheet() {
        final String className = controller.getActiveClass();
        if (className == null) {
            return;
        }
        final var sheet = CLASS_MAP.get(className);
        final var setting = Filter.fromToggleGroup(filter, complete, present, absent);
        final var roster = Classes.CLASS_INFO.get(className);
        for (String fullName : NAME_LIST) {
            final String[] split = Utilities.extractWords(fullName);
            processName(split, roster, sheet, setting);
        }
    }

    public void loadAttendanceSheet(File file) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            NAME_LIST.clear();

            while (reader.ready()) {
                final String line = reader.readLine();
                if (line.isEmpty()) {
                    continue;
                }
                final String name = line.substring(0, line.indexOf(','));
                if (name.contains("(")) {
                    continue;
                }
                NAME_LIST.add(name);
            }
            loaded.setText(file.getPath());
            updateAttendanceSheet();
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

    public void updateAttendanceMap() {
        final String className = controller.getActiveClass();
        if (className == null) {
            return;
        }
        final var presentMap = CLASS_MAP.computeIfAbsent(className, map -> new HashMap<>());
        for (var student : Classes.CLASS_INFO.get(className)) {
            presentMap.computeIfAbsent(student, map -> {
                final var property = new SimpleBooleanProperty(false);
                property.addListener((observableValue, old, checked) -> {
                    final var setting = Filter.fromToggleGroup(filter, complete, present, absent);
                    if (checked && setting == Filter.ABSENT || !checked && setting == Filter.PRESENT) {
                        students.remove(student);
                    }
                });
                return property;
            });
        }
    }

    @FXML
    public void loadSheet() {
        final LoadFiles loader = new LoadFiles(false);
        loader.display(App.STAGE_STACK.peek());
        final File[] chosen = loader.getFiles();

        if (chosen != null && chosen.length > 0) {
            loadAttendanceSheet(chosen[0]);
        }
    }

    public void updateNames() {
        clearDisplay();
        final var setting = Filter.fromToggleGroup(filter, complete, present, absent);
        final var roster = getRoster(controller, setting);
        if (roster != null) {
            students.addAll(roster);
        }
    }

    public void clearDisplay() {
        students.clear();
    }

    public static ArrayList<Student> getRoster(PrimaryController controller, Filter filter) {
        final String className = controller.getActiveClass();
        if (className == null) {
            return null;
        }
        final var roster = Classes.CLASS_INFO.get(className);
        final ArrayList<Student> marked = new ArrayList<>();
        final var presentMap = CLASS_MAP.get(className);
        for (var student : roster) {
            final var present = presentMap.get(student).getValue();
            if (filter == Filter.COMPLETE || !present && filter == Filter.ABSENT || present && filter == Filter.PRESENT) {
                marked.add(student);
            }
        }
        return marked;
    }

    public static void processName(String[] split, ArrayList<Student> roster, Map<Student, SimpleBooleanProperty> sheet, Filter filter) {
        final String[] reversed = Arrays.copyOf(split, split.length);
        Utilities.reverseArray(reversed);

        for (var student : roster) {
            if (matchStudent(split, student) || matchStudent(reversed, student)) {
                sheet.get(student).set(true);
                return;
            }
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
}
