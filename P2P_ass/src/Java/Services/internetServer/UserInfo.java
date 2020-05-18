package Java.Services.internetServer;

import Java.Services.User.FriendInfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class UserInfo implements Serializable {
    private String userName;
    private String passWord;
    private String IP;
    private int port;
    private ArrayList<FriendInfo> friend_list = new ArrayList<FriendInfo>();

    public ArrayList<FriendInfo> getFriendList() {
        return friend_list;
    }

    public void setFriendList(ArrayList<FriendInfo> friend_list) {
        this.friend_list = friend_list;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public UserInfo() {

    }

    public UserInfo(String user_name, String pass, String ip) {
        this.userName = user_name;
        this.passWord = pass;
        this.IP = ip;
        this.port = ThreadLocalRandom.current().nextInt(8000, 65000 + 1);
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String user_name) {
        this.userName = user_name;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String pass_word) {
        this.passWord = pass_word;
    }

    public String getIP() {
        return IP;
    }

    public void setIP(String ip) {
        this.IP = ip;
    }
}