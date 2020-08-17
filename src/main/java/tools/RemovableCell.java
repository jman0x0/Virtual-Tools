package tools;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

class RemovableCell extends ListCell<String> {
    private static final Image TRASH = new Image(RemovableCell.class.getResourceAsStream("trash.png"));
    private final HBox box = new HBox();
    private final Label label = new Label();
    private final Button button = new Button();
    private final TextField textField = new TextField();

    public RemovableCell() {
        super();
        label.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(label, Priority.ALWAYS);
        box.getChildren().addAll(label, button);
        button.setGraphic(new ImageView(TRASH));
        textField.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if(event.getCode().equals(KeyCode.ENTER)) {
                    commitEdit(textField.getText());
                }
            }
        });

        textField.focusedProperty().addListener((observableValue, old, focused) -> {
            if (!focused) {
                var text = textField.getText();
                startEdit();
                commitEdit(text);
            }
        });

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
        else if (isEditing()) {
            textField.setText(data);
        }
        else {
            label.setText(data);
            setGraphic(box);
        }
    }

    @Override
    public void startEdit() {
        super.startEdit();
        textField.setText(label.getText());
        setGraphic(textField);
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        setGraphic(box);
    }

    @Override
    public void commitEdit(String data) {
        super.commitEdit(data);
        setItem(textField.getText());
        setGraphic(box);
    }
}
