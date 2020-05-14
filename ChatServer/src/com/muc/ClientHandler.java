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

    private XML xml = new XML();

    // State of processing
    private String state = "LOGIN";

    // Prototype of user data
    private UserInfo userInfo = new UserInfo();

    // Prototype of friend list
    private ArrayList<UserInfo> friendList = new ArrayList<UserInfo>();

    // Initialize all class
    private Systematic systematic = new Systematic();
    private HandleUserRequest handleUserRequest = new HandleUserRequest();
    private Friend friend = new Friend();
    private HandlePoolRequest handlePoolRequest = new HandlePoolRequest();
    private FileTransfer fileTransfer = new FileTransfer();

    public ClientHandler(Server server, Socket clientSocket) throws IOException {
        this.server = server;
        this.clientSocket = clientSocket;
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        out = new PrintWriter(clientSocket.getOutputStream(), true);
    }

    // Used in add and remove functions in the server current user list
    private ClientHandler getClientHandler() {
        return this;
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
        // Handler user cmd
        String clientInput;
        while(((clientInput = in.readLine()) != null) || !state.equals("END")) {
            String[] tokens = StringUtils.split(clientInput);
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
                            default:
                                String notification = "You must login first " + cmd;
                                out.println(notification);
                                break;
                        }
                        break;

                    case "OPERATION":
                        switch (cmd) {
                            case "quit":
                                handleUserRequest.handleLogOff();
                                state = "END";
                                break;
                            case "list":
                                systematic.displayAllOnlineClients(server.getUserList());
                                break;
                            case "friend":
                                friend.displayFriendStatusList(server.getUserList());
                                break;
                            case "add":
                                handlePoolRequest.addFriend(server.getUserList(),tokens);
                                break;
                            case "view":
                                handlePoolRequest.handleRequest(server.getRequestPool(), server.getUserList());
                                break;
                            case "sendfile":
                                // Transfer file to other user
                                handlePoolRequest.sendFile(server.getUserList() ,tokens);
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


    private class FileTransfer {

        private  void transferFile(File myFile) throws IOException {
            byte[] fileBuffer = new byte[(int) myFile.length()];
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(myFile));
            bis.read(fileBuffer, 0, fileBuffer.length);
            OutputStream os = clientSocket.getOutputStream();
            os.write(fileBuffer, 0, fileBuffer.length);
            os.flush();
        }
    }

    private class Systematic {
        public UserInfo getUserInfo() {
            return userInfo;
        }

        private boolean isUserInFriendList(String friend) {
            for(UserInfo name : friendList) {
                if(name.getUserName().equals(friend)) return true;
            }
            return false;
        }

        private void broadcastStatus(ArrayList<ClientHandler> userList, String status) {
            // Get username of current user
            String user_username = userInfo.getUserName();
            // Loop over the users
            for(ClientHandler user : userList) {
                boolean friendFound = user.systematic.isUserInFriendList(user_username);
                if(friendFound == true) {
                    String onl = user_username + " is now " + status;
                    user.out.println(onl);
                }
            }
        }

        private void displayAllOnlineClients(ArrayList<ClientHandler> userList) {
            String user_userName = userInfo.getUserName();
            // send a list of online users to current user
            for(ClientHandler user : userList) {
                String all_userName = user.systematic.getUserInfo().getUserName();
                if(!user_userName.equals(all_userName)) {
                    String msg = "online " + all_userName;
                    out.println(msg);
                }
            }
        }
    }

    private class HandleUserRequest{
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
                    UserInfo newUser = new UserInfo(newUserName, newPassword);
                    server.addUserInfo(newUserName, newUser);
                    out.println("OK signup");
                }
                else {
                    out.println("Please choose another username");
                }
            }
            else {
                String msg = "Invalid input";
                out.println(msg);
            }
        }

        private void handleLogOff() throws IOException {
            server.removeUser(getClientHandler());
            systematic.broadcastStatus(server.getUserList(), "offline");
            // Store all the friend made to the xml file
            xml.Encoder(server.getUserPath(), userInfo, friendList);
            System.out.println("User " + userInfo.getUserName() + " has disconnect");
        }

        private boolean handleLogin(HashMap<String, UserInfo> dataInfo , ArrayList<ClientHandler> userList, String[] tokens) throws IOException {
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
                    if(user.systematic.getUserInfo().getUserName().equals(input_userName)) {
                        isDupplicated = true;
                    }
                }

                // Display result and handle login functions
                if(isCreated == true && isDupplicated == false) {
                    userInfo.setUserName(input_userName);
                    userInfo.setPassWord(input_passWord);
                    out.println("OK login");

                    // send msg to server
                    System.out.println("User " + userInfo.getUserName() + " has login " + new Date());

                    // read all the friend of the current user
                    ArrayList<UserInfo> getList = new ArrayList<UserInfo>();
                    xml.readFileFriend(server.getUserPath(), userInfo, friendList, "Friend");
                    System.out.println(friendList);

                    // broadcast offline to friend list
                    systematic.broadcastStatus(server.getUserList(), "online");
                    server.addUser(getClientHandler());
                    return true;

                } else if(isCreated == false){
                    out.println("Account not existed or password wrong");
                    return false;
                } else if(isDupplicated == true) {
                    out.println("The account is already logged in");
                    return false;
                }
            }
            else {
                out.println("Invalid parameters");
            }
            return false;
        }
    }

    private class Friend {

        public ArrayList<UserInfo> getFriendList() {
            return friendList;
        }

        public void clientAddFriend(UserInfo friendInfo) {
            friendList.add(friendInfo);
        }

        public void clientRemoveFriend(UserInfo friendInfo) {
            friendList.remove(friendInfo);
        }

        private void displayFriendStatusList(ArrayList<ClientHandler> userList) throws IOException {
            for(UserInfo friend : friendList) {
                String friend_userName = friend.getUserName();
                boolean friendIsOnline = false;
                for(ClientHandler user : userList) {
                    if(friend_userName.equals(user.systematic.getUserInfo().getUserName())) {
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
    }

    private class HandlePoolRequest {
        private boolean checkIfInPool(HashMap<String, UserInfo> incomingRequest, String sender, String receiver, String requestType, String fileName) {
            for(String i : incomingRequest.keySet()) {
                String[] getRequestUser = i.split(":", 4);
                String requestUser = getRequestUser[0];
                String responseUser = incomingRequest.get(i).getUserName();
                String typeOfRequest = getRequestUser[2];
                String requestContent = getRequestUser[3];

                if(requestUser.equals(sender) && responseUser.equals(receiver) && typeOfRequest.equals(requestType)) {
                    if(requestType.equals("Friend")) return true;
                    else if(requestType.equals("File") && requestContent.equals(fileName)) return true;
                }
            }
            return false;
        }

        private void sendFile(ArrayList<ClientHandler> userList, String[] tokens) throws IOException {
            if(tokens.length == 3) {
                String dir = tokens[1];
                System.out.println(dir);
                String receiver = tokens[2];
                String user_userName = userInfo.getUserName();
                // Check if you send to yourself
                if(user_userName.equals(receiver)) {
                    out.println("You cannot send to your self");
                    return;
                }
                // Check if you already request to send the file to same user
                boolean isInPool = checkIfInPool(server.getRequestPool(), user_userName, receiver, "File", dir);
                if(isInPool == true) {
                    out.println("You already request to send the file to this user");
                    return;
                }
                // Find the receiver
                for(ClientHandler user : userList) {
                    if(user.systematic.getUserInfo().getUserName().equals(receiver)) {
                        out.println("File request has been sent");
                        user.out.println(user_userName + " sent you a file");
                        int id = server.getPoolID();
                        server.setPoolID(id + 1);
                        server.addRequest(user_userName + ":" + id + ":File:" + dir, user.systematic.getUserInfo());
                    }
                }
            } else {
                out.println("Invalid parameter");
            }
        }

        private void addFriend(ArrayList<ClientHandler> userList, String[] tokens) throws IOException {
            if (tokens.length == 2) {
                String user_userName = userInfo.getUserName();
                String friendToAdd = tokens[1];
                // Check if you add yourself
                if(friendToAdd.equals(user_userName)) {
                    out.println("You cannot add yourself");
                    return;
                }
                // Check if you request the same person in the pool
                boolean isInPool = checkIfInPool(server.getRequestPool(), user_userName, friendToAdd, "Friend", "No");
                if(isInPool == true) {
                    out.println("You already add this user. Please wait for the reply");
                    return;
                }
                // Get request in the pool
                boolean isFriendExisted = systematic.isUserInFriendList(friendToAdd);
                if (isFriendExisted == false) {
                    for (ClientHandler user : userList) {
                        if (friendToAdd.equals(user.systematic.getUserInfo().getUserName())) {
                            int id = server.getPoolID();
                            server.setPoolID(id + 1);
                            server.addRequest(user_userName + ":" + id + ":Friend:no", user.systematic.getUserInfo());
                            System.out.println(server.getRequestPool());

                            // send to the requestUser
                            out.println("Add request has been sent");

                            // send to the responseUser
                            user.out.println("You got a new friend request");
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
                out.println("Invalid parameter");
            }
        }

        private void handleRequest(HashMap<String, UserInfo> incomingRequest, ArrayList<ClientHandler> userList) throws IOException {
            String user_userName = userInfo.getUserName();
            // Loop in the pool check for add friend request
            int count = 0;
            for(String i : incomingRequest.keySet()) {
                // Get requestUser and responseUser
                System.out.println(i);
                String[] getRequestUser = i.split(":", 4);
                String requestUser = getRequestUser[0];
                String responseUser = incomingRequest.get(i).getUserName();
                System.out.println(requestUser + " + " + responseUser);

                // Get type of request
                String typeRequest = getRequestUser[2];
                // Get request content
                String dir = getRequestUser[3];

                // Check if the responseUser is the user
                if(user_userName.equals(responseUser)) {
                    count += 1;
                    if(typeRequest.equals("Friend")) {
                        String msg = requestUser + " wants to add you(Yes/No)";
                        out.println(msg);
                    }
                    else if(typeRequest.equals("File")) {
                        String msg = requestUser + " send you a file(Yes/No)";
                        out.println(msg);
                    }

                    String response = "";
                    while((response = in.readLine()) != null) {
                        if(response.equalsIgnoreCase("yes")) {
                            for(ClientHandler user : userList) {
                                if(user.systematic.getUserInfo().getUserName().equals(requestUser)) {
                                    if(typeRequest.equals("Friend")) {
                                        // add responseUser to requestUser list
                                        user.friend.clientAddFriend(incomingRequest.get(i));
                                        String accept = user_userName + " accept your request";
                                        user.out.println(accept);

                                        // add requestUser to responseUser list
                                        friend.clientAddFriend(user.systematic.getUserInfo());
                                        String yes_msg = requestUser + " added to your list";
                                        out.println(yes_msg);
                                        break;
                                    }
                                    else if(typeRequest.equals("File")) {
                                        // get the file transferred
                                        String prefix = "sending:" + dir;
                                        out.println(prefix);
                                        //fileTransfer.transferFile(dir);
                                        File file = new File(dir);
                                        fileTransfer.transferFile(file);
                                        // wait for the file to be transferred
                                        String waitForResponse;
                                        while((waitForResponse = in.readLine()) !=null) {
                                            if(waitForResponse.equals("OK fileTransfer")){
                                                out.println("Get file success");
                                                break;
                                            }
                                        }
                                        // send msg to the requestUser
                                        String accept = user_userName + " accept the file";
                                        user.out.println(accept);
                                    }
                                }
                            }
                            break;

                        } else if(response.equalsIgnoreCase("no")) {
                            for(ClientHandler user : userList) {
                                if(user.systematic.getUserInfo().getUserName().equals(requestUser)) {
                                    if(typeRequest.equals("Friend")) {
                                        // send refuse msg to requestUser
                                        String refuse = user_userName + " refuse your request";
                                        user.out.println(refuse);

                                        // send refuse msg to responseUser
                                        String no_msg = "You declined " + requestUser + " request";
                                        out.println(no_msg);
                                        break;
                                    }
                                    else if(typeRequest.equals("File")) {
                                        // send refuse msg to requestUser
                                        String refuse = user_userName + " refuse your file";
                                        user.out.println(refuse);

                                        // send refuse msg to responseUser
                                        String no_msg = "You declined " + requestUser + " file";
                                        out.println(no_msg);
                                        break;
                                    }
                                }
                            }
                            break;
                        } else {
                            out.println("Please type yes or no");
                        }
                    }
                }
            }
            if(count > 0) {
                // Clear all request pool
                server.removeRequest(userInfo);
                System.out.println(server.getRequestPool());
            } else {
                out.println("No request found");
            }
        }
    }

}
