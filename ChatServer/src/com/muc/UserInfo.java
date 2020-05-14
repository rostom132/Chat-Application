package com.muc;

public class UserInfo {
    private String userName;
    private String passWord;

    public UserInfo() {
    }

    public UserInfo(String user_name, String pass) {
        this.userName = user_name;
        this.passWord = pass;
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
