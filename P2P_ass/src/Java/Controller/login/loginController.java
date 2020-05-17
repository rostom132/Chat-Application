package Java.Controller.login;

import Java.Controller.dragScene;
import Java.Controller.main.mainController;
import Java.Services.ClientServer.Server;
import Java.Services.internetServer.UserInfo;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.stage.StageStyle;


import java.io.IOException;
import java.util.ArrayList;

public class loginController {

    private int port_number;
    Stage stage;
    @FXML
    TextField user_name;
    TextField password;

    public void setPort(int port){
        this.port_number = port;
    }
    @FXML
    public void Close(ActionEvent event){
        stage = (Stage) ((Button)event.getSource()).getScene().getWindow();
        stage.close();
    }

    public  void temp(){
        System.out.println("day la cua so moi");
    }

    @FXML
    public  void login(ActionEvent event) throws IOException {
        FXMLLoader loginSucess = new FXMLLoader(getClass().getResource("/Resources/views/mainUI.fxml"));
        Parent root1 = (Parent) loginSucess.load();
        mainController user_remote = loginSucess.getController();
        logIn(user_name,user_remote,port_number);
        Stage mainScene = (Stage) (Stage) ((Node)event.getSource()).getScene().getWindow();
        mainScene.setScene(new Scene(root1));
        dragScene.dragWindow(root1,mainScene);
        mainScene.show();
    }

    @FXML
    private void onKeyPressed(KeyEvent event) throws IOException {
        if (event.getCode() == KeyCode.ENTER) {
            FXMLLoader loginSucess = new FXMLLoader(getClass().getResource("/Resources/views/mainUI.fxml"));
            Parent root1 = (Parent) loginSucess.load();
            mainController user_remote = loginSucess.getController();
            logIn(user_name,user_remote,port_number);
            Stage mainScene = (Stage) (Stage) ((Node)event.getSource()).getScene().getWindow();
            mainScene.setScene(new Scene(root1));
            dragScene.dragWindow(root1,mainScene);
            mainScene.show();
        }
    }

    public static void logIn( TextField user_name_field,mainController user_remote, int port_number) throws IOException {
        String name = user_name_field.getText();
        new Server(name, user_remote, port_number);
        ArrayList<UserInfo> temp = new ArrayList<UserInfo>();
        temp.add(new UserInfo("Tien","127.0.0.1",8181));
        temp.add(new UserInfo("Khoa","127.0.0.1",9000));
        temp.add(new UserInfo("Thien", "127.0.0.1", 100));
        user_remote.addAllFriends(temp);
    }
}

