package tools;

import java.util.HashMap;
import java.util.Map;

public class Notebook {
    private final Map<Student, String> notes = new HashMap<>();

    void clear() {
        notes.clear();
    }

    int size() {
        return notes.size();
    }

    String getNote(Student student) {
        final String note = notes.get(student);
        return note == null ? "" : note;
    }

    void setNote(Student student, String data) {
        notes.put(student, data);
    }
}
