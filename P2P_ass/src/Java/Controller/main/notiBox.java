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

public class  notiBox {

    //Create variable
    public static void displayNoti (String title, String noti) {
        Platform.runLater(() -> {
            Stage window = new Stage();
            window.initModality(Modality.APPLICATION_MODAL);
            window.getIcons().add(new Image("/Resources/images/Picture1.png"));
            window.setTitle(title);
            window.setMinWidth(350);
            Label label = new Label();
            label.setText(noti);

            //Create two buttons
            Button okButton = new Button("OK");

            //Clicking will set answer and close window
            okButton.setOnAction(e -> {
                window.close();
            });

            VBox layout = new VBox(10);

            //Add buttons
            layout.getChildren().addAll(label, okButton);
            layout.setAlignment(Pos.CENTER);
            Scene scene = new Scene(layout);
            window.setScene(scene);
            window.showAndWait();
        });
    }

}
