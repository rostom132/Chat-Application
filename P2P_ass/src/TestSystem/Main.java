package TestSystem;

import Java.Controller.dragScene;
import Java.Services.ClientServer.ClientHandler;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import Java.Controller.login.loginController;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Resources/views/LoginUI.fxml"));
        Parent root = loader.load();
        loginController controllerfirst = loader.getController();
        InetAddress ip = null;
        try {
            ip = InetAddress.getByName("192.168.0.106");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        controllerfirst.currentClient = new ClientHandler(ip, 8818);
        controllerfirst.currentClient.getState().addListener(controllerfirst.login_confirm);
        primaryStage.getIcons().add(new Image("/Resources/images/Picture1.png"));
        primaryStage.setTitle("Login Chatapp");
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
