package internetServer;

public class FriendInfo {

    private String friend_name;
    private String status;

    public FriendInfo(String friend_name, String status) {
        this.friend_name = friend_name;
        this.status = status;
    }

    public String getFriendName() {
        return friend_name;
    }

    public void setFriendName(String friend_name) {
        this.friend_name = friend_name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
