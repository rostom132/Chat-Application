package internetServer;

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

    private XML xml = new XML();

    // State of processing
    private String state = "LOGIN";

    // Prototype of user data
    private UserInfo userInfo;

    // Prototype of friend list
    private ArrayList<FriendInfo> friendList = new ArrayList<FriendInfo>();

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
            e.printStackTrace();
        }
    }

    private void handleClientSocket() throws IOException, InterruptedException {
        // Handler user cmd
        while(!state.equals("END") && (!(clientInput = dis.readUTF()).equals(""))) {
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
                            case "friend":
                                System.out.println(userInfo.getUserName() + ": " + friendList);
                                break;
                            case "add":
                                handlePoolRequest.addFriend(server.getUserList(),tokens);
                                break;
                            case "remove":
                                friend.clientRemoveFriend(server.getUserList(), tokens);
                                break;
                            case "view":
                                handlePoolRequest.handleRequest(server.getRequestPool(), server.getUserList());
                                break;
                            case "sendfile":
                                handlePoolRequest.sendFile(server.getUserList() ,tokens);
                                break;
                            case "delete":
                                handleUserRequest.deleteAccount(server.getUserList());
                                state = "END";
                                dos.writeUTF("end");
                                break;
                            default:
                                String notification = "Unknown command " + cmd;
                                dos.writeUTF(notification);
                                break;
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
            for(FriendInfo friend : friendList) {
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

        public void updateFriendProperty(ArrayList<ServerHandler> userList, boolean status, String property) throws IOException {
            // Get username of current user
            String user_username = userInfo.getUserName();
            String user_ip = userInfo.getIP();
            // Update the current online user
            for(ServerHandler user : userList) {
                // Check if this user is in friend list
                boolean friendFound = user.systematic.isUserInFriendList(user_username);
                if(friendFound) {
                    int index = user.friend.getFriendIndex(user_username);
                    if(property.equals("Status")) {
                        user.friendList.get(index).setStatus(status);
                        System.out.println(user.friendList.get(index));
                        user.dos.writeUTF("status " + user_username + " " + status);
                    }
                    else if(property.equals("IP")) {
                        System.out.println(user.friendList.get(index));
                        user.friendList.get(index).setFriendIP(user_ip);
                        user.dos.writeUTF("ip " + user_username + " " + user_ip);
                        return;
                    }
                }
            }
            // Update status to the offline user
            // --> Read in the xml file and update all the user in the database
            List<String> updateFile = xml.getXMLFile();
            System.out.println("File read for update" + server.getXMLFile());
            for(String s : server.getUserInfo().keySet()) {
                if(!s.equals(user_username)) {
                    boolean isUserOnline = systematic.isUserOnline(server.getUserList(), s);
                    if (!isUserOnline) {
                        UserInfo offline_user = server.getUserInfo().get(s);
                        System.out.println("Offline: " + offline_user.getUserName());
                        xml.interactFriendContainer(updateFile, offline_user, user_username, status, "Update");
                    }
                }
            }
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
            oos.writeObject(friendList);
            System.out.println("Friend list sent");
        }

        public void transferFile(File my_file) throws IOException {
            if (!my_file.exists() || !my_file.isFile()) return;
            System.out.println(my_file.getName());
            int fileSize;
            fileSize = (int)my_file.length();
            System.out.println(fileSize);
            byte b[] = new byte[fileSize];
            FileInputStream fis = new FileInputStream(my_file);
            BufferedInputStream bis = new BufferedInputStream(fis);

            dos.writeUTF(Integer.toString(fileSize));
            System.out.println(Integer.toString(fileSize));
            // Wait for confirmation
            if (dis.readUTF().equals("OK")) {
                System.out.println("OK check file size is done");
                bis.read(b, 0, b.length);
                OutputStream os = clientSocket.getOutputStream();
                os.write(b, 0, b.length);
                System.out.println("Done send");
                os.flush();
            }
            while(true) {
                if(dis.readUTF().equals("Done")) break;
            }
        }
    }

    class HandleUserRequest{

        public void handleSignUp(HashMap<String, UserInfo> dataInfo, String[] tokens) throws IOException {
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
                if(signUpSuccess) {
                    UserInfo newUser = new UserInfo(newUserName, newPassword, clientSocket.getInetAddress().toString());
                    // Use mutex lock to protect the hashmap
                    mutex.lock();
                        server.addUserInfo(newUserName, newUser);
                    mutex.unlock();
                    // Write the userInfo to the xml file
                    xml.Encoder(newUser, friendList);
                    dos.writeUTF("OK signup");
                } else {
                    dos.writeUTF("Please choose another username");
                }
            }
            else {
                dos.writeUTF("Invalid input");
            }
        }

        public boolean handleLogin(HashMap<String, UserInfo> dataInfo , ArrayList<ServerHandler> userList, String[] tokens) throws IOException {
            if(tokens.length == 3) {
                String input_userName = tokens[1];
                String input_passWord = tokens[2];

                boolean isCreated = false;
                boolean isDupplicated = false;

                // Check inputInfo with dataInfo
                for(String i : dataInfo.keySet()) {
                    if(i.equals(input_userName) && (dataInfo.get(i)).getPassWord().equals(input_passWord)) {
                        userInfo = dataInfo.get(i);
                        System.out.println(userInfo);
                        isCreated = true;
                    }
                }

                // Check for duplicate clients
                for(ServerHandler user : userList) {
                    if(user.systematic.getUserInfo().getUserName().equals(input_userName)) {
                        isDupplicated = true;
                    }
                }

                // Display result and handle login functions
                if(isCreated && !isDupplicated) {
//                    System.out.println(server.syncTest);
                    // Send msg to server
                    System.out.println("User " + userInfo.getUserName() + " has login " + new Date());

                    // Read all the friend of the current user
                    System.out.println(server.getXMLFile());
                    xml.readFileFriend(server.getXMLFile(), userInfo, friendList);
                    System.out.println("FriendList: " + friendList);

                    // Signal the local user to receiver friend list
                    dos.writeUTF("login");

                    // Send the friendList to the local user
                    systematic.sendCurrentFriendList();

                    // Broadcast online to friend list
                    systematic.updateFriendProperty(server.getUserList(), true, "Status");

                    // Update this userIP to all of user friends
                    systematic.updateFriendProperty(server.getUserList(), true, "IP");

                    // Use mutex lock to protect the userList
                    mutex.lock();
                        server.addUser(getClientHandler());
                    mutex.unlock();
                    return true;

                } else if(!isCreated){
                    dos.writeUTF("Account not existed or password wrong");
                    return false;
                } else if(isDupplicated) {
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

            // Add all the friends to the xml file
            xml.Encoder(userInfo, friendList);

            // Broadcast online to friend list
            systematic.updateFriendProperty(server.getUserList(), false, "Status");
        }

        public void deleteAccount(ArrayList<ServerHandler> userList) throws IOException {
            // Remove user from the current userList
            mutex.lock();
                server.removeUser(getClientHandler());
            mutex.unlock();
            // Remove this user from the friendList of online users
            for(ServerHandler user : userList) {
                user.friend.removeFriendByName(userInfo.getUserName());
            }
            // Remove in the XML file
            for(String s : server.getUserInfo().keySet()) {
                if(!s.equals(userInfo.getUserName())) {
                    UserInfo userInfo = server.getUserInfo().get(s);
                    xml.interactFriendContainer(server.getXMLFile(), userInfo, userInfo.getUserName(), false, "Remove");
                }
                else server.removeUserInfo(userInfo.getUserName());
            }
            System.out.println(server.getUserInfo());
            // Delete the file
            xml.removeUser(server.getXMLFile(), userInfo);
        }
    }

    class Friend {

        public void addFriend(FriendInfo new_friend) {
            friendList.add(new_friend);
        }

        public FriendInfo getFriendInfo(int index) {
            FriendInfo friendFound = friendList.get(index);
            return friendFound;
        }


        public int getFriendIndex(String friend_name) {
            int index = 0;
            for(FriendInfo friend : friendList) {
                if(friend.getFriendName().equals(friend_name)) {
                    index = friendList.indexOf(friend);
                    break;
                }
            }
            System.out.println(index);
            return index;
        }

        public void removeFriendByName(String remove_name) {
            for(FriendInfo friend : friendList) {
                if(friend.getFriendName().equals(remove_name)) {
                    friendList.remove(friend);
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
                    System.out.println(friendList);
                    // Remove case: the removeUser is currently online
                    for(ServerHandler user : userList) {
                        if(user.userInfo.getUserName().equals(removeName)) {
                            // Remove the friend in the responseUser
                            user.friend.removeFriendByName(userInfo.getUserName());
                            System.out.println(user.friendList);
                            user.dos.writeUTF("remove " + userInfo.getUserName());
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
                    List<String> updateFile = xml.getXMLFile();
                    xml.interactFriendContainer(updateFile, offline_user, userInfo.getUserName(), false, "Remove");
                    System.out.println("Remove offline successfully");
                }
            } else {
                dos.writeUTF("Invalid parameter");
            }
        }
    }

    private class HandlePoolRequest {

        public boolean checkIfInPool(HashMap<String, UserInfo> incomingRequest, String sender, String receiver, String requestType, String fileName) {
            for(String i : incomingRequest.keySet()) {
                String[] get_request_format = i.split(":", 4);

                String requestUser = get_request_format[0];

                String response_user = incomingRequest.get(i).getUserName();

                String request_type = get_request_format[2];

                String request_content = get_request_format[3];

                if(requestUser.equals(sender) && response_user.equals(receiver) && request_type.equals(requestType)) {
                    if(requestType.equals("Friend")) return true;
                    else if(requestType.equals("File") && request_content.equals(fileName)) return true;
                }
            }
            return false;
        }

        private void sendFile(ArrayList<ServerHandler> userList, String[] tokens) throws IOException {
            if(tokens.length == 3) {
                String dir = tokens[1];
                System.out.println(dir);
                String receiver = tokens[2];
                String user_userName = userInfo.getUserName();
                // Check if you send to yourself
                if(user_userName.equals(receiver)) {
                    dos.writeUTF("You cannot send to your self");
                    return;
                }
                // Check if you already request to send the file to same user
                boolean isInPool = checkIfInPool(server.getRequestPool(), user_userName, receiver, "File", dir);
                if(isInPool) {
                    dos.writeUTF("You already request to send the file to this user");
                    return;
                }
                // Find the receiver
                for(ServerHandler user : userList) {
                    if(user.systematic.getUserInfo().getUserName().equals(receiver)) {
                        dos.writeUTF("File request has been sent");
                        user.dos.writeUTF(user_userName + " sent you a file");
                        int id = server.getPoolID();
                        // Use mutex lock to protect the pool id and request pool
                        mutex.lock();
                            server.setPoolID(id + 1);
                            server.addRequest(user_userName + ":" + id + ":File:" + dir, user.systematic.getUserInfo());
                        mutex.unlock();
                    }
                }
            } else {
                dos.writeUTF("Invalid parameter");
            }
        }

        private void addFriend(ArrayList<ServerHandler> userList, String[] tokens) throws IOException {
            if (tokens.length == 2) {
                String user_userName = userInfo.getUserName();
                String friendToAdd = tokens[1];
                // Check if you add yourself
                if(friendToAdd.equals(user_userName)) {
                    dos.writeUTF("You cannot add yourself");
                    return;
                }
                // Check if you request the same person in the pool
                boolean isInPool = checkIfInPool(server.getRequestPool(), user_userName, friendToAdd, "Friend", "No");
                if(isInPool) {
                    dos.writeUTF("You already add this user. Please wait for the reply");
                    return;
                }
                // Get request in the pool
                boolean isFriendExisted = systematic.isUserInFriendList(friendToAdd);
                if (!isFriendExisted) {
                    for (ServerHandler user : userList) {
                        if (friendToAdd.equals(user.systematic.getUserInfo().getUserName())) {
                            int id = server.getPoolID();
                            // Use mutex lock to protect the pool id and request pool
                            mutex.lock();
                                server.setPoolID(id + 1);
                                server.addRequest(user_userName + ":" + id + ":Friend:no", user.systematic.getUserInfo());
                            mutex.unlock();
                            dos.writeUTF("Request has been sent");
                            System.out.println(server.getRequestPool());
                            // send to the requestUser
                            // send to the responseUser
                            user.dos.writeUTF("You got a new friend request");
                            return;
                        }
                    }
                    dos.writeUTF("No user name " + friendToAdd + " found");
                } else {
                    dos.writeUTF(friendToAdd + " is already added");
                }
            } else {
                dos.writeUTF("Invalid parameter");
            }
        }

        private void handleRequest(HashMap<String, UserInfo> incomingRequest, ArrayList<ServerHandler> userList) throws IOException {
            // Get info of user
            String user_userName = userInfo.getUserName();
            String user_IP = userInfo.getIP();
            // Loop in the pool check for add friend request
            int count = 0;
            for(String i : incomingRequest.keySet()) {
                // Get requestUser and responseUser
                String[] get_request = i.split(":", 4);
                String request_user_name = get_request[0];
                String response_user_name = incomingRequest.get(i).getUserName();

                // Get type of request
                String typeRequest = get_request[2];
                // Get request content
                String dir = get_request[3];

                // Check if the responseUser is the user
                if(user_userName.equals(response_user_name)) {
                    count += 1;
                    if(typeRequest.equals("Friend")) {
                        dos.writeUTF(request_user_name + " wants to add you(Yes/No)");
                    }
                    else if(typeRequest.equals("File")) {
                        dos.writeUTF(request_user_name + " send you a file(Yes/No))");
                    }
                    // Get the requestUser
                    ServerHandler requestUser = null;
                    for(ServerHandler user : userList) {
                        if(user.systematic.getUserInfo().getUserName().equals(request_user_name)) {
                            requestUser = user;
                            break;
                        }
                    }
                    if(requestUser == null) {
                        dos.writeUTF("Request user has offline");
                        break;
                    }
                    String response = "";
                    while(!(response = dis.readUTF()).equals("")) {
                        if(response.equalsIgnoreCase("yes")) {
                            switch (typeRequest) {
                                case "Friend": {
                                    // Add response_user to request_user list
                                    FriendInfo response_user = new FriendInfo(response_user_name, true, user_IP);
                                    requestUser.friend.addFriend(response_user);
                                    // Signal the response_user to add to local list friend
                                    requestUser.dos.writeUTF("add " + response_user_name + " true " + user_IP);
                                    System.out.println(request_user_name + " " + requestUser.friendList);

                                    // Add request_user to response_user list
                                    FriendInfo request_user = new FriendInfo(request_user_name, true, requestUser.userInfo.getIP());
                                    friend.addFriend(request_user);
                                    // Signal the request_user to add to local list friend
                                    dos.writeUTF("add " + request_user_name + " true " + requestUser.userInfo.getIP());
                                    System.out.println(response_user_name + " " + friendList);
                                    break;
                                }
                                case "File": {
                                    // Get the file transferred
                                    String prefix = "sending " + dir;
                                    System.out.println(prefix);
                                    dos.writeUTF(prefix);
                                    // Send the file to the user
                                    File file = new File(dir);
                                    System.out.println(file);
                                    systematic.transferFile(file);
                                    break;
                                }
                            }
                            break;

                        } else if(response.equalsIgnoreCase("no")) {
                            switch (typeRequest) {
                                case "Friend": {
                                    // Send refuse msg to requestUser
                                    requestUser.dos.writeUTF(user_userName + " refuse your request");
                                    // Send refuse msg to responseUser
                                    dos.writeUTF("You declined " + request_user_name + " request");
                                    break;
                                }
                                case "File": {
                                    // Send refuse msg to requestUser
                                    requestUser.dos.writeUTF(user_userName + " refuse your file");
                                    // Send refuse msg to responseUser
                                    dos.writeUTF("You declined " + request_user_name + " file");
                                    break;
                                }
                            }
                            break;
                        } else {
                            dos.writeUTF("Please type yes or no");
                        }
                    }
                }
            }
            if(count > 0) {
                // Clear all request pool
                // User mutex lock to protect the request pool
                mutex.lock();
                    server.removeRequest(userInfo);
                mutex.unlock();
                System.out.println(server.getRequestPool());
            } else {
                dos.writeUTF("No request found");
            }
        }
    }

}
