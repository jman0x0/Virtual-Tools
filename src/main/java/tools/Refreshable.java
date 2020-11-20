package tools;

import java.time.LocalDate;
import java.util.EnumSet;

public interface Refreshable {
    default void refresh(PrimaryController controller) {
        final var changes = getControllerState().update(controller);
        processChanges(changes);
    }

    default void processChanges(EnumSet<ControllerState.Change> changes) {
        if (changes.contains(ControllerState.Change.CLASSROOM)) {
            classChanged(getControllerState().getClassroom());
        }
        if (changes.contains(ControllerState.Change.ORDER)) {
            orderChanged(getControllerState().getOrder());
        }
        if (changes.contains(ControllerState.Change.DATE)) {
            dateChanged(getControllerState().getDate());
        }
    }

    default void setClass(Classroom classroom) {
        getControllerState().setClassroom(classroom);
        classChanged(classroom);
    }

    default void setOrder(PrimaryController.Order order) {
        getControllerState().setOrder(order);
        orderChanged(order);
    }

    default void setDate(LocalDate date) {
        getControllerState().setDate(date);
        dateChanged(date);
    }

    void classChanged(Classroom classroom);

    void orderChanged(PrimaryController.Order order);

    void dateChanged(LocalDate date);

    ControllerState getControllerState();
}
