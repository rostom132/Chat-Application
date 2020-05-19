package Java.Services.User;

import Java.Controller.chat.chatRoom;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class OwnerInfo {
    private String userName;
    private String IP;
    private int port;
    private ObservableList<chatRoom> friend_list = FXCollections.observableArrayList(chat -> new Observable[]{chat.getNumUnseenMess(), chat.getOnline()});

    public OwnerInfo() {

    }

    public ObservableList<chatRoom> getFriendList(){
        return this.friend_list;
    }

//    public void setFriendList(ArrayList<FriendInfo> temp){
//        this.friend_list = temp;
//    }

    public void addNewFriend(chatRoom new_friend){
        this.friend_list.add(new_friend);
    }


    public int getPort() {
        return this.port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public OwnerInfo(String user_name, String ip) {
        this.userName = user_name;
        this.IP = ip;
        this.port = ThreadLocalRandom.current().nextInt(8000, 65000 + 1);
    }

    public OwnerInfo(String user_name, String ip, int port){
        this.userName = user_name;
        this.IP = ip;
        this.port = port;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String user_name) {
        this.userName = user_name;
    }


    public String getIP() {
        return IP;
    }

    public void setIP(String ip) {
        this.IP = ip;
    }
}
