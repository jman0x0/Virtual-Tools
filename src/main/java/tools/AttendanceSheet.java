package tools;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

public class AttendanceSheet {
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

    private final Map<Student, SimpleBooleanProperty> sheet = new HashMap<>();

    private BiConsumer<Student, Boolean> callback;

    private final ArrayList<Student> roster;

    public AttendanceSheet(ArrayList<Student> roster) {
        this.roster = roster;
    }

    private SimpleBooleanProperty fetch(Student student) {
        final var property = sheet.computeIfAbsent(student, create -> {
            final var bool = new SimpleBooleanProperty(false);
            bool.addListener(((observableValue, aBoolean, t1) -> {
                if (callback != null) {
                    callback.accept(student, t1);
                }
            }));
            return bool;
        });
        return property;
    }

    public void mark(Student student, boolean value) {
        fetch(student).set(value);
    }

    public SimpleBooleanProperty get(Student student) {
        return fetch(student);
    }

    public int size() {
        return sheet.size();
    }

    public void setOnStudentMarked(BiConsumer<Student, Boolean> callback) {
        this.callback = callback;
    }

    public void checkAll() {
        for (Student student : roster) {
            mark(student, true);
        }
    }

    public void clear() {
        sheet.clear();
    }

    public Set<Map.Entry<Student, SimpleBooleanProperty>> entrySet() {
        return sheet.entrySet();
    }

    public ArrayList<Student> getRoster(Filter filter) {
        if (filter == Filter.COMPLETE) {
            return new ArrayList<>(roster);
        }
        final ArrayList<Student> marked = new ArrayList<>();
        for (var student : roster) {
            final var property = sheet.get(student);
            final boolean present = property != null && property.get();
            if (!present && filter == Filter.ABSENT || present && filter == Filter.PRESENT) {
                marked.add(student);
            }
        }
        return marked;
    }
}
