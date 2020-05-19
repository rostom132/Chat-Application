package Java.Controller.main;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.geometry.*;

import javax.swing.text.Style;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class addFriendBox{

    //Create variable
    static String friend_name;

    public static String addFriend() {
        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.getIcons().add(new Image("/Resources/images/Picture1.png"));
        window.setTitle("Add New Friend");
        window.setMinWidth(450);
        Label label = new Label();
        label.setText("Insert the name of your friend!");
        System.out.println("aloalo");
        //Create two buttons
        TextField name = new TextField();
        name.setAlignment(Pos.CENTER);
        name.maxWidth(200);
        Button ok = new Button("Find Friend");

        //Clicking will set answer and close window
        ok.setOnAction(e -> {
            friend_name = name.getText();
            window.close();
        });

        VBox layout = new VBox(10);
        layout.addEventHandler(KeyEvent.KEY_PRESSED, ev -> {
            if (ev.getCode() == KeyCode.ENTER) {
                ok.fire();
                ev.consume();
            }
        });
        //Add buttons
        layout.getChildren().addAll(label,name,ok );
        layout.setAlignment(Pos.CENTER);
        Scene scene = new Scene(layout);
        window.setScene(scene);
        window.showAndWait();

        //Make sure to return answer
        return friend_name;
    }

}
