package internetServer;

import java.io.Serializable;

public class FriendInfo implements Serializable {

    private String friendName;
    private boolean status;
    private String friendIP;

    public FriendInfo() {

    }

    public FriendInfo(String friend_name, boolean status, String friend_IP) {
        this.friendName = friend_name;
        this.status = status;
        this.friendIP = friend_IP;
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
