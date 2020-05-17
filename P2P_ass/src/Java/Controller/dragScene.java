package Java.Controller;

import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class dragScene {

    private static double dragX = 0;
    private static double dragY = 0;
    public  static void dragWindow(Parent root, Stage stage) {
        root.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                // record a delta distance for the drag and drop operation.
                dragX = mouseEvent.getSceneX();
                dragY = mouseEvent.getSceneY();
            }
        });
        root.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                stage.setX(mouseEvent.getScreenX() - dragX);
                stage.setY(mouseEvent.getScreenY() + dragY);
            }
        });
    }
}
