package tools;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Objects;

public class ControllerState {
    public enum Change {
        CLASSROOM,
        ORDER,
        DATE
    }

    private Classroom classroom;
    private PrimaryController.Order order;
    private LocalDate date;

    public EnumSet<Change> update(PrimaryController controller) {
        final EnumSet<Change> changes = EnumSet.noneOf(Change.class);
        final var currentClassroom = controller.getActiveClassroom();
        final var currentOrder = controller.getOrder();
        final var currentDate = controller.getActiveDate();

        if (!Objects.equals(classroom, currentClassroom)) {
            classroom = currentClassroom;
            changes.add(Change.CLASSROOM);
        }
        if (!Objects.equals(order, currentOrder)) {
            order = currentOrder;
            changes.add(Change.ORDER);
        }
        if (!Objects.equals(date, currentDate)) {
            date = currentDate;
            changes.add(Change.DATE);
        }

        return changes;
    }

    public void setClassroom(Classroom classroom) {
        this.classroom = classroom;
    }

    public void setOrder(PrimaryController.Order order) {
        this.order = order;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Classroom getClassroom() {
        return classroom;
    }

    public PrimaryController.Order getOrder() {
        return order;
    }

    public LocalDate getDate() {
        return date;
    }
}
