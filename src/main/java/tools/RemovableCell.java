package tools;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

class RemovableCell extends ListCell<String> {
    private static final Image TRASH = new Image(RemovableCell.class.getResourceAsStream("trash.png"));
    private final HBox box = new HBox();
    private final Label label = new Label();
    private final Button button = new Button();

    public RemovableCell() {
        super();
        label.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(label, Priority.ALWAYS);
        box.getChildren().addAll(label, button);
        button.setGraphic(new ImageView(TRASH));
        button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                getListView().getItems().remove(label.getText());
            }
        });
    }

    @Override
    protected void updateItem(String data, boolean empty) {
        super.updateItem(data, empty);
        setText(null);
        if (empty) {
            setGraphic(null);
        }
        else {
            label.setText(data);
            setGraphic(box);
        }
    }
}
