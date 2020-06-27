package Java.Controller.main;

import Java.Controller.chat.chatRoom;
import Java.Controller.dragScene;
import Java.Controller.login.loginController;
import Java.Services.ClientServer.ClientHandler;
//import animatefx.animation.FadeInDownBig;
import TestSystem.Main;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ResourceBundle;


public class mainUIController implements Initializable {
    private BooleanProperty Server_online_status = new SimpleBooleanProperty(false);
    public ClientHandler currentClient;
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

    ChangeListener<Number> listenerAccept = new ChangeListener<Number>() {
        @Override
        public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
            // value changed
            if (newValue.intValue() == 1) {
                Platform.runLater(() -> {
                    messView.setCenter(currentUser.messList);
                    currentUser.setChatting(true);
                });
            } else if (newValue.intValue() == 2){
                Platform.runLater(() -> {
                    messView.setCenter(currentUser.rec_file);
                });
            } else if (newValue.intValue() == 0){
                Platform.runLater(() -> {
                    messView.setCenter(currentUser.requestChat);
                });
            }

//            if (!Server_online_status.get()){
//                Platform.runLater(() -> {
//                    messView.setCenter(currentUser.requestChat);
//                    currentUser.setChatting(false);
//                    currentUser.chatError("offline");
//                });
//            }

        }
    };

    public ChangeListener<Number> mainUIcontrol = new ChangeListener<Number>() {
        @Override
        public void changed(ObservableValue<? extends Number> observable, Number integer, Number t1) {
             switch (t1.intValue()){
                 case 2:
                     notiBox.displayNoti("Add success!" , currentClient.getOwnerInfo().getFriendList().get(currentClient.getOwnerInfo().getFriendList().size()-1).getGuestName() + " now be your friend!");
                     currentClient.state_Client.set(1);
                     currentClient.getState().set(1);
                     break;
                 case 3:
                     Platform.runLater(() ->{
                         messView.setCenter(null);
                         friend_name.setText("");
                     });
                     currentClient.getState().set(1);
                     break;
                 case 7:
                     if (confirmBox.checkConfirm("New Friend request", "Do you want to add " + currentClient.request_add_user +" ?")) {
                         currentClient.sendMess("answer_add " + currentClient.request_add_user + " yes");
                     }
                     else {
                         currentClient.sendMess("answer_add " + currentClient.request_add_user + " no");
                     }
                     currentClient.state_Client.set(1);
                     break;
                 case 9:
                     notiBox.displayNoti("Server Response!", currentClient.server_noti);
                     currentClient.state_Client.set(1);
                     break;
            }
        }
    };

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.send.setOnAction(this::sendMessage);
        listOfFriends.setCellFactory(param -> new Cell());
        listOfFriends.prefHeight(50);
    }

    @FXML
    public void handleAddFriend(MouseEvent arg){
        String temp = addFriendBox.addRemoveFriend("Add new friend!");
        currentClient.sendMess("add " + temp);
    }

    @FXML
    public void handleRemoveFriend(MouseEvent arg) {
        String temp = addFriendBox.addRemoveFriend("Delete new friend!");
        currentClient.sendMess("remove " + temp);
    }

    @FXML
    public void handlelogout(MouseEvent arg) throws IOException{
        currentClient.sendMess("quit");
        Server_online_status.set(false);
        for (chatRoom friend:currentClient.getOwnerInfo().getFriendList()){
            if(friend.getChatAccept().get() == 1 || friend.getChatAccept().get() == 2)friend.offRoom();
        }
        FXMLLoader login = new FXMLLoader(getClass().getResource("/Resources/views/LoginUI.fxml"));
        Parent root1 = (Parent) login.load();
        loginController controllerfirst = login.getController();
        this.currentClient.getState().removeListener(mainUIcontrol);
        InetAddress ip = null;
        try {
            ip = InetAddress.getByName(Main.IP);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        controllerfirst.currentClient =  new ClientHandler(ip, Main.PORT);
        controllerfirst.currentClient.getState().addListener(controllerfirst.login_confirm);
        Stage mainScene = (Stage) (Stage) ((Node)arg.getSource()).getScene().getWindow();
        mainScene.setScene(new Scene(root1));
        dragScene.dragWindow(root1,mainScene);
        mainScene.show();
    }

    @FXML
    public void handleMouseClick(MouseEvent arg0) {
        if (currentUser != null)  {
            currentUser.setChatting(false);
            currentUser.getChatAccept().removeListener(listenerAccept);
        }
        currentUser = listOfFriends.getSelectionModel().getSelectedItem();
        if (currentUser == null) return;
        currentUser.getChatAccept().addListener(listenerAccept);
        if (currentUser.getChatAccept().get() == 1){
            currentUser.setChatting(true);
            currentUser.resetUnseenMess();
            messView.setCenter(currentUser.messList);
        } else if (currentUser.getChatAccept().get() == 0){
            messView.setCenter(currentUser.requestChat);
        } else if(currentUser.getChatAccept().get() == 2){
            messView.setCenter(currentUser.rec_file);
        }
        changeFriend(currentUser.getGuestName());
    }

    @FXML
    private void onKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (currentUser == null) return;
            String message = inputMess.getText();
            if (message.equals("")) return;
            currentUser.sendMess(message);
            inputMess.clear();
        }
    }

    private void sendMessage(ActionEvent e) {
        if (currentUser == null) return;
        String message = inputMess.getText();
        if (message.equals("")) return;
        currentUser.sendMess(message);
        inputMess.clear();
    }

    @FXML
    public void  handleSendFile(MouseEvent event) {
        if (currentUser != null && currentUser.getChatAccept().get()==1) {
            FileChooser fileChooser = new FileChooser();
            configureFileChooser(fileChooser);
            File file = fileChooser.showOpenDialog((Stage) ((Node) event.getSource()).getScene().getWindow());
//            try {
            Thread temp = new Thread() {
                public void run() {
                    try {
                        currentUser.getCurrentChat().transferFile(file);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            temp.start();
        }
    }

    private static void configureFileChooser(FileChooser fileChooser) {
        fileChooser.setTitle("Choose File to send");
        fileChooser.setInitialDirectory(
                new File(System.getProperty("user.home"))
        );
    }

    static class Cell extends ListCell<chatRoom>{
        ImageView online_icon_pallet = new ImageView(new Image("Resources/images/user.png",22,22,false,false));
        ImageView user_pallet =  new ImageView(new Image("Resources/images/user.png",22,22,false,false));
        Pane offline = new Pane();
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
            Platform.runLater(() ->{
                setText(null);
                setGraphic(null);
            });
            if(friend!= null && !empty){
                receive_mess.setText(String.valueOf(friend.getNumUnseenMess().get()));
                receive_mess.setStyle("-fx-text-fill: white; -fx-font-size: 10px");
                user_name.setText(friend.getGuestName());
                user_name.setStyle("-fx-text-fill: white; -fx-font-size: 12px");
                if (friend.getOnline().get()) {
                    online_icon_pallet.setImage(new Image("Resources/images/omg.png",12,12,false,false));
                }else {
                    online_icon_pallet.setImage(new Image("Resources/images/iconoff.png",12,12,false,false));
                }
                Platform.runLater(() ->{
                    setGraphic(box);
                });

            }
        }
    }
    public void startController(ClientHandler currentClient){
        this.currentClient = currentClient;
        this.user_name = this.currentClient.getOwnerInfo().getUserName();
        this.listOfFriends.setItems(this.currentClient.getOwnerInfo().getFriendList());
        User.setText(user_name);
        Server_online_status.set(true);
    }

//    public void addAllFriends(ArrayList<FriendInfo> friend_list){
//        for (FriendInfo friend:friend_list){
////            friendList.add(new chatRoom(user_name,friend.getUserName(),friend.getIp(),friend.getPort()));
//            friend_list.add(new chatRoom(user_name, friend.getFriendName(),friend.getFriendIP(),friend.getPort(),friend.getStatus()));
//        }
//    }

    public void newChatRoom(String user_name, Socket connection, DataInputStream data_in, DataOutputStream data_out){
        int index_chatroom = 0;
        for(chatRoom temp_room:currentClient.getOwnerInfo().getFriendList()){
            if (temp_room.getGuestName().equals(user_name)){
                temp_room.getStartChat(connection,data_in,data_out);
                if (currentUser != null && currentUser.getChatAccept().get() == 1)  {
                    currentUser.setChatting(false);
                    currentUser.getChatAccept().removeListener(listenerAccept);
                }
                currentUser = temp_room;
                currentUser.getChatAccept().addListener(listenerAccept);
                if (currentUser.getChatAccept().get() == 1){
                    currentUser.setChatting(true);
                    currentUser.resetUnseenMess();
                    messView.setCenter(currentUser.messList);
                } else {
                    messView.setCenter(currentUser.requestChat);
                }
                changeFriend(currentUser.getGuestName());
                listOfFriends.scrollTo(index_chatroom);
                listOfFriends.getSelectionModel().select(index_chatroom);
                break;
            }
            index_chatroom ++;
        }
    }



    private void changeFriend(String name){
        friend_name.setText(name);
        friend_name.setStyle("-fx-background-color: #7c68e7; -fx-background-radius: 0");
        friend_name.setAlignment(Pos.CENTER);
//        new FadeInDownBig(friend_name).play();
    }

    public BooleanProperty getServerOnlineStatus(){
        return this.Server_online_status;
    }
}
