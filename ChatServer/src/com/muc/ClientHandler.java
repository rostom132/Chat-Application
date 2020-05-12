package com.muc;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.Socket;
import java.util.*;

public class ClientHandler extends  Thread{
    private final Socket clientSocket;
    private final Server server;
    private final BufferedReader in;
    private final PrintWriter out;
    private String state = "LOGIN";

    private UserInfo userInfo = new UserInfo();

    private ArrayList<UserInfo> friendList = new ArrayList<UserInfo>(); // Temporary

    public ClientHandler(Server server, Socket clientSocket) throws IOException {
        this.server = server;
        this.clientSocket = clientSocket;
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        out = new PrintWriter(clientSocket.getOutputStream(), true);
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public ArrayList<UserInfo> getFriendList() {
        return friendList;
    }

    public void clientAddFriend(UserInfo friendInfo) {
        friendList.add(friendInfo);
    }

    @Override
    public void run() {
        try {
            handleClientSocket();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            out.close();
            try {
                in.close();
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleClientSocket() throws IOException, InterruptedException {
        String clientInput;

        while(((clientInput = in.readLine()) != null) && !state.equals("END")) {
            String[] tokens = StringUtils.split(clientInput);
            if(tokens != null && tokens.length > 0) {
                String cmd = tokens[0].toLowerCase();
                switch (state) {
                    case "LOGIN":
                        switch (cmd) {
                            case "signup":
                                handleSignUp(server.getUserInfo(), tokens);
                                break;
                            case "login":
                                handleLogin(server.getUserInfo(),server.getUserList(),tokens);
                                break;
                            default:
                                String notification = "You must login first " + cmd;
                                out.println(notification);
                                break;
                        }
                        break;

                    case "OPERATION":
                        switch (cmd) {
                            case "quit":
                                handleLogOff();
                                break;
                            case "list":
                                displayAllOnlineClients(server.getUserList());
                                break;
                            case "friend":
                                displayFriendStatusList(server.getUserList());
                                break;
                            case "add":
                                addFriend(server.getUserList(),tokens);
                                break;
                            case "view":
                                handleAddFriendRequest(server.getRequestPool(), server.getUserList());
                                break;
                            default:
                                String notification = "Unknown command " + cmd;
                                out.println(notification);
                                break;
                        }
                        break;
                }
            }
        }
    }

    private void displayFriendStatusList(ArrayList<ClientHandler> userList) throws IOException {
        for(UserInfo friend : friendList) {
            String friend_userName = friend.getUserName();
            boolean friendIsOnline = false;
            for(ClientHandler user : userList) {
                if(friend_userName.equals(user.getUserInfo().getUserName())) {
                    String foundOnl = friend_userName + " online";
                    out.println(foundOnl);
                    friendIsOnline = true;
                    break;
                }
            }
            if(friendIsOnline == false) {
                String foundOff = friend_userName + " offline";
                out.println(foundOff);
            }
        }
    }

    private void broadcastStatus(ArrayList<ClientHandler> userList, String status) {
        // Get username of current user
        String user_username = userInfo.getUserName();
        // Loop over the users
        for(ClientHandler user : userList) {
            boolean friendFound = user.isUserInFriendList(user_username);
            if(friendFound == true) {
                String onl = user_username + " is now " + status;
                user.out.println(onl);
            }
        }
    }

    private void handleAddFriendRequest(HashMap<String, UserInfo> incomingRequest, ArrayList<ClientHandler> userList) throws IOException {
        String user_userName = userInfo.getUserName();
        // Loop in the pool check for add friend request
        int count = 0;
        for(String i : incomingRequest.keySet()) {
            // Get requestUser and responseUser
            System.out.println(i);
            String[] getRequestUser = i.split(":", 2);
            String requestUser = getRequestUser[0];
            String responseUser = incomingRequest.get(i).getUserName();

            System.out.println(requestUser + " + " + responseUser);
            // Check if the responseUser is the user
            if(user_userName.equals(responseUser)) {
                count += 1;
                String msg = requestUser + " wants to add you(Yes/No)";
                out.println(msg);

                String response = "";
                while((response = in.readLine()) != null) {
                    if(response.equalsIgnoreCase("yes")) {
                        for(ClientHandler user : userList) {
                            if(user.getUserInfo().getUserName().equals(requestUser)) {
                                // add responseUser to requestUser list
                                user.clientAddFriend(incomingRequest.get(i));
                                String accept = user_userName + " accept your request";
                                user.out.println(accept);

                                // add requestUser to responseUser list
                                clientAddFriend(user.getUserInfo());
                                String yes_msg = requestUser + " added to your list";
                                out.println(yes_msg);
                                break;
                            }
                        }
                        break;

                    } else if(response.equalsIgnoreCase("no")) {
                        for(ClientHandler user : userList) {
                            if(user.getUserInfo().getUserName().equals(requestUser)) {
                                String refuse = user_userName + " refuse your request";
                                user.out.println(refuse);
                                break;
                            }
                        }
                        break;
                    } else {
                        String notification = "Please type yes or no";
                        out.println(notification);
                    }
                }
            }
        }
        if(count > 0) {
            // Clear all request pool
            server.removeRequest(userInfo);
            System.out.println(server.getRequestPool());
        } else {
            String notfound = "No request found";
            out.println(notfound);
        }
    }

    private boolean isUserInFriendList(String friend) {
        for(UserInfo name : friendList) {
            if(name.getUserName().equals(friend)) return true;
        }
        return false;
    }

    private boolean checkIfInPool(HashMap<String, UserInfo> incomingRequest, String sender, String receiver) {
        for(String i : incomingRequest.keySet()) {
            String[] getRequestUser = i.split(":", 2);
            String requestUser = getRequestUser[0];
            String responseUser = incomingRequest.get(i).getUserName();
            if(requestUser.equals(sender) && responseUser.equals(receiver)) {
                return true;
            }
        }
        return false;
    }

    private void addFriend(ArrayList<ClientHandler> userList, String[] tokens) throws IOException {
        if (tokens.length == 2) {
            String user_userName = userInfo.getUserName();
            String friendToAdd = tokens[1];
            // Check if you add yourself
            if(friendToAdd.equals(user_userName)) {
                String self = "You cannot add yourself";
                out.println(self);
                return;
            }
            // Check if you request the same person in the pool
            boolean isInPool = checkIfInPool(server.getRequestPool(), user_userName, friendToAdd);
            if(isInPool == true) {
                String self = "You already add this user. Please wait for the reply";
                out.println(self);
                return;
            }
            // Get request in the pool
            boolean isFriendExisted = isUserInFriendList(friendToAdd);
            if (isFriendExisted == false) {
                for (ClientHandler user : userList) {
                    if (friendToAdd.equals(user.getUserInfo().getUserName())) {
                        int id = server.getPoolID();
                        server.setPoolID(id + 1);
                        server.addRequest(user_userName + ":" + id, user.getUserInfo());
                        System.out.println(server.getRequestPool());

                        // send to the requestUser
                        String request = "Request has sent";
                        out.println(request);

                        // send to the responseUser
                        String response = "You got a new friend request";
                        user.out.println(response);
                        return;
                    }
                }
                String msg = "No user name " + friendToAdd + " found";
                out.println(msg);
            } else {
                String fail = friendToAdd + " is already added";
                out.println(fail);
            }
        } else {
            String msg = "Invalid parameter";
            out.println(msg);
        }
    }

    private void displayAllOnlineClients(ArrayList<ClientHandler> userList) {
        String user_userName = userInfo.getUserName();
        // send a list of online users to current user
        for(ClientHandler user : userList) {
            String all_userName = user.getUserInfo().getUserName();
            if(!user_userName.equals(all_userName)) {
                String msg = "online " + all_userName;
                out.println(msg);
            }
        }
    }

    private void handleSignUp(HashMap<String, UserInfo> dataInfo, String[] tokens) throws IOException {
        if(tokens.length == 3) {
            String newUserName = tokens[1];
            String newPassword = tokens[2];
            boolean signUpSuccess = true;
            for(String i : dataInfo.keySet()) {
                if(newUserName.equals(i)) {
                    signUpSuccess = false;
                    break;
                }
            }
            if(signUpSuccess == true) {
                server.addUserInfo(newUserName, userInfo);
                server.Encoder(newUserName, newPassword, 0);
                String msg = "OK";
                out.println(msg);
            }
            else {
                String msg = "Please choose another username";
                out.println(msg);
            }
        }
        else {
            String msg = "Invalid input";
            out.println(msg);
        }
    }

    private void handleLogOff() throws IOException {
        server.removeUser(this);
        broadcastStatus(server.getUserList(), "offline");
        System.out.println("User " + userInfo.getUserName() + " has disconnect");
        state = "END";
    }

    private void handleLogin(HashMap<String, UserInfo> dataInfo , ArrayList<ClientHandler> userList, String[] tokens) throws IOException {
        if(tokens.length == 3) {
            String input_userName = tokens[1];
            String input_passWord = tokens[2];

            boolean isCreated = false;
            boolean isDupplicated = false;

            // Check inputInfo with dataInfo
            for(String i : dataInfo.keySet()) {
                if(i.equals(input_userName) && (dataInfo.get(i)).getPassWord().equals(input_passWord)) {
                    isCreated = true;
                }
            }

            // Check for duplicate clients
            for(ClientHandler user : userList) {
                if(user.getUserInfo().getUserName().equals(input_userName)) {
                    isDupplicated = true;
                }
            }

            // Disp result
            if(isCreated == true && isDupplicated == false) {
                userInfo.setUserName(input_userName);
                userInfo.setPassWord(input_passWord);
                String msg = "OK login";
                out.println(msg);

                // send msg to server
                System.out.println("User " + userInfo.getUserName() + " has login " + new Date());

                // broadcast offline to friend list
                broadcastStatus(server.getUserList(), "online");
                server.addUser(this);
                state = "OPERATION";

            } else if(isCreated == false){
                String msg = "Account not existed or password wrong";
                out.println(msg);
            } else if(isDupplicated == true) {
                String msg = "The account is already logged in";
                out.println(msg);
            }
        }
    }
}
