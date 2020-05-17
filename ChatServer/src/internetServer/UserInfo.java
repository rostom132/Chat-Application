package internetServer;

public class UserInfo {
    private String userName;
    private String passWord;
    private String IP;

    public UserInfo() {

    }

    public UserInfo(String user_name, String pass, String ip) {
        this.userName = user_name;
        this.passWord = pass;
        this.IP = ip;
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
