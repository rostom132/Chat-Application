package TestSystem;

import Java.Controller.dragScene;
import Java.Controller.login.loginController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main1 extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Resources/views/LoginUI.fxml"));
        Parent root = loader.load();
        loginController controllerfirst = loader.getController();
        controllerfirst.setPort(9000);
        primaryStage.setScene(new Scene(root, Color.TRANSPARENT));
        primaryStage.initStyle(StageStyle.UNDECORATED);

        controllerfirst.temp();
        dragScene.dragWindow(root,primaryStage);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
