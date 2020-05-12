package com.muc;

public class UserInfo {
    private String userName;
    private String passWord;
    private int IP;

    public UserInfo() {
    }

    public UserInfo(String user_name, String pass, int ip) {
        this.userName = user_name;
        this.passWord = pass;
        this.IP = ip;
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

    public int getPort() {
        return IP;
    }

    public void setPort(int port) {
        IP = port;
    }

}
