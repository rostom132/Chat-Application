package Java.Services.User;

import java.io.Serializable;

public class FriendInfo implements Serializable{

    private String friendName;
    private boolean status;
    private String friendIP;
    private  int port;

    public FriendInfo() {

    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public FriendInfo(String friend_name, boolean status, String friend_IP, int port) {
        this.friendName = friend_name;
        this.status = status;
        this.friendIP = friend_IP;
        this.port = port;
    }

    public String getFriendName() {
        return friendName;
    }

    public void setFriendName(String friendName) {
        this.friendName = friendName;
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getFriendIP() {
        return friendIP;
    }

    public void setFriendIP(String friendIP) {
        this.friendIP = friendIP;
    }
}