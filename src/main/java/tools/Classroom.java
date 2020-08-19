package tools;

import java.util.ArrayList;
import java.util.Iterator;

public class Classroom implements Iterable<Student> {
    private String name;

    private final ArrayList<Student> students = new ArrayList<>();

    private final AttendanceSheet attendanceSheet = new AttendanceSheet(students);

    public Classroom(String name) {
        this.name = name;
    }

    public boolean remove(Student student) {
        return students.remove(student);
    }

    public void add(Student student) {
        students.add(student);
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Student> getStudents() {
        return students;
    }

    public void setStudents(ArrayList<Student> students) {
        this.students.clear();
        this.students.addAll(students);
        attendanceSheet.clear();
    }

    public int size() {
        return students.size();
    }

    public String getName() {
        return name;
    }

    public AttendanceSheet getAttendanceSheet() {
        return attendanceSheet;
    }

    @Override
    public Iterator<Student> iterator() {
        return students.iterator();
    }
}
