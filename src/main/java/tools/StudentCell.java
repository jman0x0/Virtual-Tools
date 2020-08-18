package tools;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.util.Callback;

public class StudentCell extends CheckBoxListCell<Student> {
    private final PrimaryController controller;

    public StudentCell(Callback<Student, ObservableValue<Boolean>> callback, PrimaryController controller) {
        super(callback);
        this.controller = controller;
    }

    @Override
    public void updateItem(Student data, boolean empty) {
        super.updateItem(data, empty);
        if (empty) {
            setText(null);
        }
        else if (controller.getOrder() == PrimaryController.Order.FIRST_LAST) {
            setText(data.getReversed());
        }
        else {
            setText(data.toString());
        }
    }
}
