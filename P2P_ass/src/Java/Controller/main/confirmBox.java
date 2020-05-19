package Java.Controller.main;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.geometry.*;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class  confirmBox {

    //Create variable
    static boolean answer;
    public static boolean checkConfirm (String title,String mess) {
        final FutureTask query = new FutureTask(new Callable() {
            @Override
            public Object call() throws Exception {
                return confirmBox.display(title, mess);
            }
        });
        Platform.runLater(query);
        boolean Confirm = false;
        try {
            Confirm = (boolean) query.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return Confirm;
    }

    private static boolean display(String title, String message) {
        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.getIcons().add(new Image("/Resources/images/Picture1.png"));
        window.setTitle(title);
        window.setMinWidth(350);
        Label label = new Label();
        label.setText(message);

        //Create two buttons
        Button yesButton = new Button("Yes");
        Button noButton = new Button("No");

        //Clicking will set answer and close window
        yesButton.setOnAction(e -> {
            answer = true;
            window.close();
        });
        noButton.setOnAction(e -> {
            answer = false;
            window.close();
        });

        VBox layout = new VBox(10);
        HBox yes_no = new HBox();
        Pane blank = new Pane();
        Pane blank_1 = new Pane();
        Pane blank_2 = new Pane();
        blank.setMinWidth(120);
        //Add buttons
        yes_no.getChildren().addAll(blank_1,yesButton,blank,noButton,blank_2);
        yes_no.setHgrow(blank_1, Priority.ALWAYS);
        yes_no.setHgrow(blank_2, Priority.ALWAYS);
        layout.getChildren().addAll(label, yes_no);
        layout.setAlignment(Pos.CENTER);
        Scene scene = new Scene(layout);
        window.setScene(scene);
        window.showAndWait();

        //Make sure to return answer
        return answer;
    }

}
