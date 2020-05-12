package com.muc;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class ServerlHandler extends  Thread{
    private final Socket clientSocket;
    private final Server server;
    private final BufferedReader in;
    private final PrintWriter out;
    private String userName;
    private String password;

    private UserInfo userInfo = new UserInfo(userName, password, 0);

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    private ArrayList<String> friendList = new ArrayList<String>(); // Temporary

    public ServerlHandler(Server server, Socket clientSocket) throws IOException {
        this.server = server;
        this.clientSocket = clientSocket;
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        out = new PrintWriter(clientSocket.getOutputStream(), true);
    }

    public String getUserName() {
        return userName;
    }

    public List<String> getFriendList() {
        return friendList;
    }

    public void clientAddFriend(String friendUserName) {
        friendList.add(friendUserName);
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
        while((clientInput = in.readLine()) != null) {
            String[] tokens = StringUtils.split(clientInput);
            if(tokens != null && tokens.length > 0) {
                String cmd = tokens[0].toLowerCase();
                switch (cmd) {
                    case "quit":
                        handleLogOff();
                        break;
                    case "signup":
                        handleSignUp(tokens);
                        break;
                    case "login":
                        handleLogin(tokens);
                        break;
                    case "list":
                        displayAllOnlineClients();
                        break;
                    case "friend":
                        friendStatus();
                        break;
                    case "add":
                        addFriend(tokens);
                        break;
                    case "view":
                        handleAddFriend();
                        break;
                    default:
                        String notification = "unknown command " + cmd;
                        out.println(notification);
                        break;
                }
            }
        }
    }

    private void friendStatus() throws IOException {
        List<ServerlHandler> clientList = server.getClientList();
        for(String friend : friendList) {
            boolean flag = false;
            for(ServerlHandler client : clientList) {
                if(friend.equals(client.getUserName())) {
                    String foundOnl = friend + " online";
                    out.println(foundOnl);
                    flag = true;
                    break;
                }
            }
            if(flag == false) {
                String foundOff = friend + " offline";
                out.println(foundOff);
            }
        }
    }

    private void broadcastFriendStatus(int num) {
        List<ServerlHandler> clientList = server.getClientList();
        for(ServerlHandler client : clientList) {
            boolean flag = client.checkFriend(userName);
            if(flag == false && num == 1) { // friend found && status: online
                String onl = userName + " is now online";
                client.out.println(onl);
            }
            else if(flag == false && num == 2) { // friend found && status: offline
                String off = userName + " is now offline";
                client.out.println(off);
            }
        }
    }

    private void handleAddFriend() throws IOException {
        // Get the current online client
        List<ServerlHandler> clientList = server.getClientList();
        // Get request from the pool
        Integer count = 0;
        HashMap<String, String> incomingRequest = server.getRequestPool();
        for(String i : incomingRequest.keySet()) {
            System.out.print(i + " + ");
            System.out.println(incomingRequest.get(i));

            String newFriend = i;
            String checkIsYou = incomingRequest.get(i);

            if(userName.equals(checkIsYou)) {
                count += 1;
                String msg = newFriend + " wants to add you(Yes/No)";
                out.println(msg);

                String response = "";
                while((response = in.readLine()) != null) {
                    if(response.equalsIgnoreCase("yes")) {
                        // add friend to request client list
                        for(ServerlHandler client : clientList) {
                            if(newFriend.equals(client.getUserName())) {
                                client.clientAddFriend(userName);
                                String accept = userName + " accept your request";
                                client.out.println(accept);
                                break;
                            }
                        }

                        // add friend to response client list
                        String yes_msg = newFriend + " added to your list";
                        out.println(yes_msg);
                        clientAddFriend(newFriend);
                        break;

                    } else if(response.equalsIgnoreCase("no")) {
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

            System.out.println(server.getRequestPool());
        } else {
            String notfound = "No request found";
            out.println(notfound);
        }
    }

    private boolean checkFriend(String friend) {
        for(String name : friendList) {
            if(name.equals(friend)) return false;
        }
        return true;
    }

    private void addFriend(String[] tokens) throws IOException {
        if (tokens.length == 2) {
            String newFriend = tokens[1];
            if(newFriend.equals(userName)) {
                String self = "You cannot add yourself";
                out.println(self);
                return;
            }
            boolean flag = checkFriend(newFriend);
            if (flag == true) {
                List<ServerlHandler> clientList = server.getClientList();
                for (ServerlHandler client : clientList) {
                    if (newFriend.equals(client.getUserName())) {
                        server.addRequest(userName, newFriend);
                        System.out.println(server.getRequestPool());

                        // send to the sender
                        String msg = "Request has sent";
                        out.println(msg);

                        // send to the recv
                        String recv = "You got a new friend request";
                        client.out.println(recv);
                        return;
                    }
                }
                String msg = "No user name " + newFriend + " found";
                out.println(msg);
            } else {
                String fail = newFriend + " is already added";
                out.println(fail);
            }
        } else {
            String msg = "Invalid parameter";
            out.println(msg);
        }
    }

    private void displayAllOnlineClients() {
        List<ServerlHandler> clientList = server.getClientList();
        // send a list of online users to current user
        for(ServerlHandler client : clientList) {
            if(client.getUserName() != null) {
                if(!userName.equals(client.getUserName())) {
                    String msg2 = "online " + client.getUserName();
                    out.println(msg2);
                }
            }
        }
    }

    private void handleSignUp(String[] tokens) throws IOException {
        if(tokens.length == 3) {
            String newUserName = tokens[1];
            String newPassword = tokens[2];
            boolean loginSuccess = true;
            HashMap<String, UserInfo> dataInfo = server.getClientInfo();
            for(String i : dataInfo.keySet()) {
                if(newUserName.equals(i)) {
                    loginSuccess = false;
                    break;
                }
            }
            if(loginSuccess == true) {
                server.addClientInfo(newUserName, userInfo);
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
        server.removeClient(this);
        broadcastFriendStatus(2);
        System.out.println("User " + userName + " has disconnect");
    }

    private void handleLogin(String[] tokens) throws IOException {
        if(tokens.length == 3) {
            String username = tokens[1];
            String password = tokens[2];
            boolean flag_login = false;
            boolean flag_dupplicate = false;
            // Check input info with dataInfo
            HashMap<String, UserInfo> dataInfo = server.getClientInfo();
            for(String i : dataInfo.keySet()) {
                if(i.equals(username) && (dataInfo.get(i)).getPassWord().equals(password)) {
                    flag_login = true;
                }
            }

            // Check for duplicate clients
            List<ServerlHandler> clientList = server.getClientList();
            for(ServerlHandler client : clientList) {
                if(username.equals(client.getUserName())) {
                    flag_dupplicate = true;
                }
            }

            // Disp result
            if(flag_login == true && flag_dupplicate == false) {
                this.userName = username;
                String msg = "OK login";
                out.println(msg);

                // send msg to server
                System.out.println("User " + userName + " has login " + new Date());

                // broadcast offline to friend list
                broadcastFriendStatus(1);
                server.addClient(this);

            } else if(flag_login == false){
                String msg = "Account not existed or password wrong";
                out.println(msg);
            } else if(flag_dupplicate == true) {
                String msg = "The account is already logged in";
                out.println(msg);
            }
        }
    }
}
