package internetServer;

import Java.Services.User.FriendInfo;
import Java.Services.User.UserInfo;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public  class ServerHandler extends Thread{
    private final Socket clientSocket;
    private final Server server;

    final DataInputStream dis;
    final DataOutputStream dos;

    private String clientInput = "";

    // Synchronize purpose
    private ReentrantLock mutex = new ReentrantLock();

    // Initialize object for reading and writing xml file
    private XML xml = new XML();

    // State of processing
    private String state = "LOGIN";

    // Prototype of user data
    private UserInfo userInfo = new UserInfo();

    // Initialize all class
    public  Systematic systematic = new Systematic();
    public  HandleUserRequest handleUserRequest = new HandleUserRequest();
    public  Friend friend = new Friend();
    public  HandlePoolRequest handlePoolRequest = new HandlePoolRequest();

    public ServerHandler(Server server, Socket clientSocket) throws IOException {
        this.server = server;
        this.clientSocket = clientSocket;
        this.dis = new DataInputStream(clientSocket.getInputStream());
        this.dos = new DataOutputStream(clientSocket.getOutputStream());
    }

    // Return the this pointer of ServerHandler
    private ServerHandler getClientHandler() {
        return this;
    }

    @Override
    public void run() {
        try {
            handleClientSocket();
            clientSocket.close();
            dis.close();
            dos.close();
        } catch (IOException | InterruptedException e) {
            // Handle the case which suddenly disconnect
            try {
                if(state.equals("OPERATION")) handleUserRequest.handleLogOff();
            } catch (IOException ex) {
                System.out.println("Error when updating status");
            }
            System.out.println(userInfo.getUserName() + " connection lost");
        }
    }

    private void handleClientSocket() throws IOException, InterruptedException {
        // Handler user cmd
        while(!state.equals("END") && (!(clientInput = dis.readUTF()).equals(""))) {
            System.out.println("User enter: " + clientInput);
            String[] tokens = StringUtils.split(clientInput);
            clientInput = "";
            if(tokens != null && tokens.length > 0) {
                String cmd = tokens[0].toLowerCase();
                switch (state) {
                    case "LOGIN":
                        switch (cmd) {
                            case "signup":
                                handleUserRequest.handleSignUp(server.getUserInfo(), tokens);
                                break;
                            case "login":
                                boolean loginSuccess = handleUserRequest.handleLogin(server.getUserInfo(),server.getUserList(),tokens);
                                if(loginSuccess == true)  state = "OPERATION";
                                break;
                            case "quit":
                                state = "END";
                                System.out.println("the end");
                                dos.writeUTF("end");
                            default:
                                dos.writeUTF("Please type signup or login or quit");
                                break;
                        }
                        break;

                    case "OPERATION":
                        switch (cmd) {
                            case "quit":
                                handleUserRequest.handleLogOff();
                                state = "END";
                                dos.writeUTF("end");
                                break;
                            case "list":
                                systematic.displayAllOnlineClients(server.getUserList());
                                break;
                            case "friend_server":
                                System.out.println("Server friendList: " + userInfo.getUserName() + ": " + userInfo.getFriendList().size());
                                break;
                            case "add":
                                handlePoolRequest.addFriendRequest(server.getUserList(),tokens);
                                break;
                            case "remove":
                                friend.clientRemoveFriend(server.getUserList(), tokens);
                                break;
                            default:
                                String notification = "Unknown command " + cmd;
                                dos.writeUTF(notification);
                                break;
                        }
                        break;

                    case "GET_RESPONSE":
                        if(tokens.length >= 3) {
                            String request_name, answer, dir;
                            request_name = tokens[1];
                            answer = tokens[2];
                            System.out.println("Response format: " + cmd + ":" + request_name + ":" + answer);
                            if (cmd.equals("answer_add")) {
                                handlePoolRequest.handleRequest(server.getUserList(), request_name, answer);
                                state = "OPERATION";
                            }
                        }
                        else {
                            dos.writeUTF("Not enough parameters");
                        }
                        break;
                }
            }
        }
    }

    class Systematic {

        public UserInfo getUserInfo() {
            return userInfo;
        }

        public boolean isUserInFriendList(String name) {
            for(FriendInfo friend : userInfo.getFriendList()) {
                if(friend.getFriendName().equals(name)) return true;
            }
            return false;
        }

        public boolean isUserOnline(ArrayList<ServerHandler> userList, String name) {
            for(ServerHandler user : userList) {
                if(user.systematic.getUserInfo().getUserName().equals(name)) {
                    return true;
                }
            }
            return false;
        }

        public void updateFriendProperty(ArrayList<ServerHandler> userList, boolean status, String case_interact) throws IOException {
            // Get username of current user
            String user_username = userInfo.getUserName();
            String user_ip = userInfo.getIP();
            // Update the current online user
            for(ServerHandler user : userList) {
                // Check if this user is in friend list
                boolean friendFound = user.systematic.isUserInFriendList(user_username);
                if(friendFound) {
                    int index = user.friend.getFriendIndex(user_username);
                    if(case_interact.equals("login")) {
                        user.userInfo.getFriendList().get(index).setStatus(status);
                        user.dos.writeUTF("status " + user_username + " " + status);
                        user.userInfo.getFriendList().get(index).setFriendIP(user_ip);
                        user.dos.writeUTF("ip " + user_username + " " + user_ip);
                    }
                    else if(case_interact.equals("quit")){
                        user.userInfo.getFriendList().get(index).setStatus(status);
                        user.dos.writeUTF("status " + user_username + " " + status);
                    }
                }
            }
            // Update status to the offline user
            // --> Read in the xml file and update all the user in the database
            mutex.lock();
            List<String> updateFile = xml.getXMLFile();
            for(String s : server.getUserInfo().keySet()) {
                if(!s.equals(user_username)) {
                    boolean isUserOnline = systematic.isUserOnline(server.getUserList(), s);
                    if (!isUserOnline) {
                        UserInfo offline_user = server.getUserInfo().get(s);
                        System.out.println("Offline: " + offline_user.getUserName());
                        xml.interactFriendContainer(updateFile, offline_user, user_username, status, user_ip, "Update", case_interact);
                    }
                }
            }
            mutex.unlock();
        }

        public void displayAllOnlineClients(ArrayList<ServerHandler> userList) throws IOException {
            String user_userName = userInfo.getUserName();
            // send a list of online users to current user
            for(ServerHandler user : userList) {
                String all_userName = user.systematic.getUserInfo().getUserName();
                if(!user_userName.equals(all_userName)) {
                    dos.writeUTF("online " + all_userName);
                }
            }
        }

        public void sendCurrentFriendList() throws IOException {
            ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
            System.out.println("Send list friend with size: " + userInfo.getFriendList());
            System.out.println(userInfo.getPort());
            oos.writeObject(userInfo);
        }
    }

    class HandleUserRequest{

        public void handleSignUp(HashMap<String, UserInfo> dataInfo, String[] tokens) throws IOException {
            if(tokens.length == 3) {
                String newUserName = tokens[1];
                String newPassword = tokens[2];
                System.out.println("New user: " + newUserName + " " + newPassword);
                boolean signUpSuccess = true;
                for(String i : dataInfo.keySet()) {
                    if(newUserName.equals(i)) {
                        signUpSuccess = false;
                        break;
                    }
                }
                if(signUpSuccess) {
                    UserInfo newUser = new UserInfo(newUserName, newPassword, clientSocket.getInetAddress().toString());
                    // Use mutex lock to protect the hashmap
                    mutex.lock();
                    server.addUserInfo(newUserName, newUser);
                    mutex.unlock();
                    // Write the userInfo to the xml file
                    xml.Encoder(newUser);
                    dos.writeUTF("Register successful");
                } else {
                    dos.writeUTF("Please choose another username");
                }
            }
            else {
                dos.writeUTF("Invalid input");
            }
        }

        public boolean handleLogin(HashMap<String, UserInfo> dataInfo , ArrayList<ServerHandler> userList, String[] tokens) throws IOException {
            if(tokens.length == 4) {
                String input_userName = tokens[1];
                String input_passWord = tokens[2];
                String user_ip = tokens[3];

                boolean isCreated = false;
                boolean isDuplicated = false;

                // Check inputInfo with dataInfo
                for(String i : dataInfo.keySet()) {
                    if(i.equals(input_userName) && (dataInfo.get(i)).getPassWord().equals(input_passWord)) {
                        isCreated = true;
                    }
                }

                // Check for duplicate clients
                for(ServerHandler user : userList) {
                    if(user.systematic.getUserInfo().getUserName().equals(input_userName)) {
                        isDuplicated = true;
                    }
                }

                // Display result and handle login functions
                if(isCreated && !isDuplicated) {
                    // Send to the user success login msg
                    dos.writeUTF("Welcome to TKT chat app");
                    // Set the information to the user
                    userInfo.setUserName(input_userName);
                    userInfo.setPassWord(input_passWord);
                    userInfo.setIP(user_ip);
                    userInfo.setPort(server.getPort(input_userName));
                    // Send msg to server
                    System.out.println("User " + userInfo.getUserName() + " has login " + new Date());

                    mutex.lock();
                    // Read all the friend of the current user
                    xml.readFileFriend(server.getXMLFile(), userInfo, userInfo.getFriendList());

                    mutex.lock();
                    // Write back with the new information
                    xml.Encoder(userInfo);
                    mutex.unlock();

                    // Broadcast online to friend list
                    systematic.updateFriendProperty(server.getUserList(), true, "login");

                    mutex.unlock();

                    // Signal the local user to receiver friend list
                    dos.writeUTF("login");

                    // Send the friendList to the local user
                    systematic.sendCurrentFriendList();

                    // Use mutex lock to protect the userList
                    mutex.lock();
                    server.addUser(getClientHandler());
                    mutex.unlock();
                    return true;

                } else if(!isCreated){
                    dos.writeUTF("Account not existed or password wrong");
                    return false;
                } else if(isDuplicated) {
                    dos.writeUTF("The account is already logged in");
                    return false;
                }
            }
            else {
                dos.writeUTF("Invalid parameters");
            }
            return false;
        }

        public void handleLogOff() throws IOException {
            // Use mutex lock to protect the userList
            mutex.lock();
            server.removeUser(getClientHandler());
            mutex.unlock();

            // Broadcast online to friend list
            systematic.updateFriendProperty(server.getUserList(), false, "quit");

            mutex.lock();
            // Add all the friends to the xml file
            xml.Encoder(userInfo);
            mutex.unlock();
        }
    }

    class Friend {

        public void addFriend(FriendInfo new_friend) {
            userInfo.getFriendList().add(new_friend);
        }

        public FriendInfo getFriendInfo(int index) {
            FriendInfo friendFound = userInfo.getFriendList().get(index);
            return friendFound;
        }

        public int getFriendIndex(String friend_name) {
            int index = 0;
            for(FriendInfo friend : userInfo.getFriendList()) {
                if(friend.getFriendName().equals(friend_name)) {
                    index = userInfo.getFriendList().indexOf(friend);
                    break;
                }
            }
            return index;
        }

        public void removeFriendByName(String remove_name) {
            for(FriendInfo friend : userInfo.getFriendList()) {
                if(friend.getFriendName().equals(remove_name)) {
                    userInfo.getFriendList().remove(friend);
                    break;
                }
            }
        }

        public void clientRemoveFriend(ArrayList<ServerHandler> userList, String[] tokens) throws IOException {
            if(tokens.length == 2) {
                // Get the responseUser to delete
                String removeName = tokens[1];
                // Check if you remove yourself
                if(userInfo.getUserName().equals(removeName)) {
                    dos.writeUTF("You cannot remove yourself");
                } else {
                    // Check if the removeFriend is in the friendList
                    boolean isFriend = systematic.isUserInFriendList(removeName);
                    if(!isFriend) {
                        dos.writeUTF("The user is not in the friend list");
                        return;
                    }
                    // Remove the friend in the requestUser
                    friend.removeFriendByName(removeName);
                    // Signal the local client to remove
                    dos.writeUTF("remove " + removeName);
                    dos.writeUTF("You remove " + removeName + " successfully");
                    // Remove case: the removeUser is currently online
                    for(ServerHandler user : userList) {
                        if(user.userInfo.getUserName().equals(removeName)) {
                            // Remove the friend in the responseUser
                            user.friend.removeFriendByName(userInfo.getUserName());
                            user.dos.writeUTF("remove " + userInfo.getUserName());
                            user.dos.writeUTF(userInfo.getUserName() + " removed you");
                            return;
                        }
                    }
                    // Remove case: the removerUser is currently offline
                    // --> Read in the xml file of requestUser and responseUser
                    UserInfo offline_user = new UserInfo();
                    for(String s : server.getUserInfo().keySet()) {
                        if(s.equals(removeName)) {
                            offline_user = server.getUserInfo().get(s);
                        }
                    }
                    System.out.println("Offline: " + offline_user.getUserName());
                    System.out.println("Online: " + userInfo.getUserName());
                    mutex.lock();
                    List<String> updateFile = xml.getXMLFile();
                    xml.interactFriendContainer(updateFile, offline_user, userInfo.getUserName(), false, "null" , "Remove", "null");
                    mutex.unlock();
                    System.out.println("Remove offline successfully");
                }
            } else {
                dos.writeUTF("Invalid parameter");
            }
        }
    }

    private class HandlePoolRequest {

        private void addFriendRequest(ArrayList<ServerHandler> userList, String[] tokens) throws IOException {
            if (tokens.length == 2) {
                String user_userName = userInfo.getUserName();
                String response_user_name = tokens[1];
                // Check if you add yourself
                if (response_user_name.equals(user_userName)) {
                    dos.writeUTF("You cannot add yourself");
                    return;
                }
                // Check if you already add this user
                boolean isFriendExisted = systematic.isUserInFriendList(response_user_name);
                if (!isFriendExisted) {
                    ServerHandler response_user = null;
                    for (ServerHandler user : userList) {
                        if (response_user_name.equals(user.systematic.getUserInfo().getUserName())) {
                            // Found the response user in the user list
                            response_user = user;
                            // Send the msg to the request user
                            dos.writeUTF("Request has been sent");
                            // Send to the response user
                            response_user.dos.writeUTF("request_add " + user_userName);
                            response_user.state = "GET_RESPONSE";
                            return;
                        }
                    }
                    dos.writeUTF("No user name " + response_user_name + " found");
                } else {
                    dos.writeUTF(response_user_name + " is already added");
                }
            } else {
                dos.writeUTF("Invalid parameter");
            }
        }

        private void handleRequest(ArrayList<ServerHandler> userList , String request_user_name, String answer) throws IOException {
            // Get the request_user information
            String response_user_name = userInfo.getUserName();
            String response_user_ip = userInfo.getIP();
            int response_user_port = userInfo.getPort();

            ServerHandler request_user = null;
            for(ServerHandler user : userList) {
                if(user.userInfo.getUserName().equals(request_user_name)) {
                    request_user = user;
                    break;
                }
            }
            if(request_user == null) {
                dos.writeUTF("Request user has offline");
                return;
            }
            String request_user_ip = request_user.userInfo.getIP();
            int request_user_port = request_user.userInfo.getPort();
            if (answer.equalsIgnoreCase("yes")) {
                // Add response_user to request_user list
                FriendInfo response_friend = new FriendInfo(response_user_name, true, response_user_ip, response_user_port);
                request_user.userInfo.getFriendList().add(response_friend);
                // Signal the response_user to add to local list friend
                request_user.dos.writeUTF("add " + response_user_name + " true " + response_user_ip + " " + response_user_port);

                // Add request_user to response_user list
                FriendInfo request_friend = new FriendInfo(request_user_name, true, request_user_ip, request_user_port);
                friend.addFriend(request_friend);
                // Signal the request_user to add to local list friend
                dos.writeUTF("add " + request_user_name + " true " + request_user_ip + " " + request_user_port);
            } else if (answer.equalsIgnoreCase("no")) {
                // Send refuse msg to requestUser
                request_user.dos.writeUTF(response_user_name + " refuse your request");
                // Send refuse msg to responseUser
                dos.writeUTF("You declined " + request_user_name + " request");
            }
        }
    }
}
