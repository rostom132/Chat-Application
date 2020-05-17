package Java.Services.internetServer;

public class UserInfo {
    private String userName;
    private String passWord;
    private boolean status;
    private String ip;
    private int port;

    public String getIp(){
        return ip;
    }

    public UserInfo(String user_name, String ip, int port) {
        this.userName = user_name;
        this.ip = ip;
        this.port = port;
//        this.status = status;
    }
    public int getPort(){
        return this.port;
    }

    public boolean getStatus(){
        return status;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }
}
