package internetServer;

public class UserInfo {
    private String userName;
    private String passWord;
    private String ip;

    public UserInfo() {

    }

    public UserInfo(String user_name, String pass, String ip) {
        this.userName = user_name;
        this.passWord = pass;
        this.ip = ip;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String user_name) {
        userName = user_name;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String pass_word) {
        passWord = pass_word;
    }
}
