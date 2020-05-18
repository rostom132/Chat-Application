package Java.Controller.login;

import Java.Controller.dragScene;
import Java.Controller.main.mainUIController;
import Java.Controller.main.notiBox;
import Java.Services.ClientServer.ClientHandler;
import Java.Services.ClientServer.ServerP2P;
import Java.Services.User.FriendInfo;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;


import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class loginController implements Initializable {

    private Stage mainscene;
    private String name;
    private String pass;
    public ClientHandler currentClient;
    private Stage stage;
    @FXML
    TextField user_name;
    @FXML
    TextField password;


    public ChangeListener<Number> login_confirm = new ChangeListener<Number>() {
        @Override
        public void changed(ObservableValue<? extends Number> observable, Number integer, Number t1) {
            if(t1.equals(1)){
                FXMLLoader loginSucess = new FXMLLoader(getClass().getResource("/Resources/views/mainUI.fxml"));
                try {
                    Parent root1 = (Parent) loginSucess.load();
                    mainUIController user_remote = loginSucess.getController();
                    new ServerP2P(name, user_remote, currentClient);
                    Platform.runLater(() -> {
                        mainscene.setTitle("Chatapp");
                        mainscene.setScene(new Scene(root1));
                        dragScene.dragWindow(root1,mainscene);
                        currentClient.getState().removeListener(login_confirm);
                        mainscene.show();
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if(t1.equals(7)){
                name = "";
                pass = "";
                notiBox.checkNoti("Wrong Password");
            }
        }
    };

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

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
        mainscene = (Stage) ((Node)event.getSource()).getScene().getWindow();
        this.logIn();
    }

    @FXML
    public void signup(ActionEvent event){
        name = user_name.getText();
        pass = password.getText();
        System.out.println(name + pass);
        currentClient.sendMess("signup " +name + " " + pass);
    }
    @FXML
    private void onKeyPressed(KeyEvent event) throws IOException {
        if (event.getCode() == KeyCode.ENTER) {
            mainscene = (Stage) ((Node)event.getSource()).getScene().getWindow();
            this.logIn();
        }
    }

    public void logIn() {
        name = user_name.getText();
        pass = password.getText();
        currentClient.sendMess("login " +name + " " + pass);
    }
}

