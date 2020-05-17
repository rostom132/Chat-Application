package Java.Controller.main;

import Java.Controller.chat.chatRoom;
import Java.Controller.dragScene;
import Java.Services.internetServer.UserInfo;
import animatefx.animation.FadeInDownBig;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;


public class mainController implements Initializable {
    ObservableList<chatRoom> friendList = FXCollections.observableArrayList(chat -> new Observable[]{chat.getNumUnseenMess()});
    private BooleanProperty Server_online_status = new SimpleBooleanProperty(false);

    chatRoom currentUser;
    private String user_name;
    @FXML
    public BorderPane messView;
    public ListView<chatRoom> listOfFriends;
    public Button send;
    public TextField inputMess;
    public Label friend_name;
    public Label User;
    public ImageView logOut;

    ChangeListener<Boolean> listenerAccept = new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
            // value changed
            if (newValue) {
                Platform.runLater(() -> {
                    messView.setCenter(currentUser.messList);
                    currentUser.setChatting(true);
                });
            } else if (Server_online_status.get()){
                Platform.runLater(() -> {
                    messView.setCenter(currentUser.requestChat);
                    currentUser.setChatting(false);
                    currentUser.chatError("offline");
                });
            }

        }
    };

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.send.setOnAction(this::sendMess);
        listOfFriends.setCellFactory(param -> new Cell());
        listOfFriends.setItems(friendList);
        listOfFriends.prefHeight(50);
    }

    @FXML
    public void handlelogout(MouseEvent arg) throws IOException{
        Server_online_status.set(false);
        for (chatRoom friend:friendList){
            if(friend.getChatAccept().get())friend.offRoom();
        }
        FXMLLoader login = new FXMLLoader(getClass().getResource("/Resources/views/LoginUI.fxml"));
        Parent root1 = (Parent) login.load();
        Stage mainScene = (Stage) (Stage) ((Node)arg.getSource()).getScene().getWindow();
        mainScene.setScene(new Scene(root1));
        dragScene.dragWindow(root1,mainScene);
        mainScene.show();
    }

    @FXML
    public void handleMouseClick(MouseEvent arg0) {
        if (currentUser != null && currentUser.getChatAccept().get())  {
            currentUser.setChatting(false);
            currentUser.getChatAccept().removeListener(listenerAccept);
        }
        currentUser = listOfFriends.getSelectionModel().getSelectedItem();
        if (currentUser == null) return;
        currentUser.getChatAccept().addListener(listenerAccept);
        if (currentUser.getChatAccept().get()){
            currentUser.setChatting(true);
            currentUser.resetUnseenMess();
            messView.setCenter(currentUser.messList);
        } else {
            messView.setCenter(currentUser.requestChat);
        }
        changeFriend(currentUser.getGuestName());
    }

    @FXML
    private void onKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (currentUser == null) return;
            String message = inputMess.getText();
            currentUser.sendMess(message);
            inputMess.clear();
        }
    }

    static class Cell extends ListCell<chatRoom>{
        ImageView online_icon_pallet = new ImageView(new Image("Resources/images/omg.png",12,12,false,false));
        ImageView user_pallet =  new ImageView(new Image("Resources/images/user.png",22,22,false,false));
        Label user_name = new Label();
        Pane blank = new Pane();
        Label receive_mess = new Label();
        VBox number = new VBox();
//        BorderPane container = new BorderPane();
        HBox box = new HBox();

        public Cell(){
            super();
            number.getChildren().addAll(online_icon_pallet,receive_mess);
            box.getChildren().addAll(user_pallet,user_name,blank,number);
            box.setHgrow(blank, Priority.ALWAYS);
        }

        public void updateItem(chatRoom friend, boolean empty){
            super.updateItem(friend,empty);
            setText(null);
            setGraphic(null);

            if(friend!= null && !empty){
                receive_mess.setText(String.valueOf(friend.getNumUnseenMess().get()));
                receive_mess.setStyle("-fx-text-fill: white; -fx-font-size: 10px");
                user_name.setText(friend.getGuestName());
                user_name.setStyle("-fx-text-fill: white; -fx-font-size: 12px");
                setGraphic(box);
            }
        }
    }
    public void startController(String user_name){
        this.user_name = user_name;
        User.setText(user_name);
        Server_online_status.set(true);
    }

    public void addAllFriends(ArrayList<UserInfo> friend_list){
        for (UserInfo friend:friend_list){
            friendList.add(new chatRoom(user_name,friend.getUserName(),friend.getIp(),friend.getPort()));
        }
    }

    public void newChatRoom(String user_name, Socket connection, DataInputStream data_in, DataOutputStream data_out){
        for(chatRoom temp_room:friendList){
            if (temp_room.getGuestName().equals(user_name)){
                temp_room.getStartChat(connection,data_in,data_out);
                if (currentUser != null && currentUser.getChatAccept().get())  {
                    currentUser.setChatting(false);
                    currentUser.getChatAccept().removeListener(listenerAccept);
                }
                currentUser = temp_room;
                currentUser.getChatAccept().addListener(listenerAccept);
                if (currentUser.getChatAccept().get()){
                    currentUser.setChatting(true);
                    currentUser.resetUnseenMess();
                    messView.setCenter(currentUser.messList);
                } else {
                    messView.setCenter(currentUser.requestChat);
                }
                changeFriend(currentUser.getGuestName());
                break;
            }
        }
    }

    private void sendMess(ActionEvent e) {
        if (currentUser == null) return;
        String message = inputMess.getText();
        currentUser.sendMess(message);
        inputMess.clear();
    }

    private void changeFriend(String name){
        friend_name.setText(name);
        friend_name.setStyle("-fx-background-color: #7c68e7; -fx-background-radius: 0");
        friend_name.setAlignment(Pos.CENTER);
        new FadeInDownBig(friend_name).play();
    }

    public BooleanProperty getServerOnlineStatus(){
        return this.Server_online_status;
    }
}
