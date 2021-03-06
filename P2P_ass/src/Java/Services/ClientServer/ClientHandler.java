package Java.Services.ClientServer;

import Java.Controller.chat.chatRoom;
import Java.Controller.main.notiBox;
import Java.Services.User.FriendInfo;
import Java.Services.User.OwnerInfo;
import Java.Services.User.UserInfo;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.apache.commons.lang3.StringUtils;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;



public class ClientHandler {

    public volatile IntegerProperty state_Client = new SimpleIntegerProperty(0);
    private final InetAddress ip;
    private final int serverPort;
    private Socket s;

    private DataInputStream dis;
    private DataOutputStream dos;

    private OwnerInfo user_info;

    public String request_add_user;
    public String file_name;
    public String server_noti;

    private boolean endConnection = false;

    public OwnerInfo getOwnerInfo(){
        return this.user_info;
    }

    public IntegerProperty getState(){
        return this.state_Client;
    }

    public void sendMess(String msg)  {
        String[] tokens = StringUtils.split(msg);
        String cmd_key = tokens[0];
        switch (cmd_key) {
            case "search":
                String searchName = tokens[1];
                searchByName(searchName);
                break;
            default:
                try {
                    dos.writeUTF(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    public ClientHandler(InetAddress ip, int serverPort) {
        this.ip = ip;
        this.serverPort = serverPort;
        clientWorking();
    }

    public void clientWorking() {
        try {
            s = new Socket(ip, serverPort);
            dis = new DataInputStream(s.getInputStream());
            dos = new DataOutputStream(s.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
            Platform.runLater(() -> {
                notiBox.displayNoti("Server is not working!", "Please wait for a few minutes!");
            });
        }
//        sendMessage.start();
        readMessage.start();
    }

    private void receiveFriendList() throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
        UserInfo temp_user = (UserInfo)ois.readObject();
//        friend_List = user_info.getFriendList();
        System.out.println(temp_user.getPort());
        user_info = new OwnerInfo(temp_user.getUserName(),temp_user.getIP(),temp_user.getPort());
        for(FriendInfo friend:temp_user.getFriendList()){
            System.out.println(friend.getFriendName() + "  " + friend.getStatus());
            user_info.addNewFriend(new chatRoom(temp_user.getUserName(), friend.getFriendName(), friend.getFriendIP(), friend.getPort(), friend.getStatus()));
        }
    }

    private void
    receiveFriendInfo(String friend_name, String statusStr, String friend_ip, int port){
        boolean status;
        if(statusStr.equals("true")) status = true;
        else status = false;
        this.user_info.addNewFriend(new chatRoom(user_info.getUserName(),friend_name,friend_ip, port, status));;
    }



    private void updateFriendStatus(String name, boolean status){
        chatRoom temp = searchByName(name);
        temp.setOnline(status);
//        friend_List.get(index).setStatus(status);
    }

    private void updateFriendIP(String name, String IP){
        chatRoom temp = searchByName(name);
        temp.setGuestIP(IP);
    }

    private chatRoom searchByName(String name){
        for (chatRoom friend:user_info.getFriendList()){
            if (friend.getGuestName().equals(name))
                return friend;
        }
        return null;
    }

    Thread readMessage = new Thread(new Runnable() {
        @Override
        public void run() {
            while(!endConnection) {
                try {
                    String serverResponse = "";
                    if(!(serverResponse = dis.readUTF()).equals(""))  {
                        System.out.println("Server says: " + serverResponse);
                        String[] tokens = StringUtils.split(serverResponse);
                        String cmd = tokens[0].toLowerCase();
                        switch (cmd) {
                            case "login":
                                dos.flush();
                                receiveFriendList();
                                state_Client.set(1);
                                break;

                            case "add":
                                String friend_name = tokens[1];
                                String statusStr = tokens[2];
                                String friend_ip = tokens[3];
                                String port = tokens[4];
                                receiveFriendInfo(friend_name, statusStr, friend_ip, Integer.parseInt(port));
                                state_Client.set(2);
                                break;

                            case "remove":
                                String remove_name = tokens[1];
                                user_info.getFriendList().removeAll(searchByName(remove_name));
                                state_Client.set(3);
                                break;

                            case "status":
                                System.out.println(tokens[1] + " online");
                                String status_name = tokens[1];
                                String status_str = tokens[2];
                                boolean status;
                                if(status_str.equals("true")) status = true;
                                else status = false;
                                updateFriendStatus(status_name, status);
                                state_Client.set(4);
                                break;

                            case "ip":
                                String ip_name = tokens[1];
                                String ip_address = tokens[2];
                                updateFriendIP(ip_name, ip_address);
                                System.out.println(ip_address);
                                state_Client.set(5);
                                break;

//                            case "sending":
//                                String fileName = tokens[1];
//                                System.out.println("FileName: " + fileName);
//                                receiveFile(fileName);
//                                state_Client.set(6);
//                                break;
                            case "request_add":
                                request_add_user = tokens[1];
                                System.out.println("add friend " + request_add_user);
                                state_Client.set(7);
                                break;
                            case "end":
                                endConnection = true;
                                s.close();
                                return;
                            case "login_error":
                                state_Client.set(8);
                                break;
                            default:
                                server_noti = serverResponse;
                                state_Client.set(9);
                                break;
                        }
                    }
                } catch (IOException | ClassNotFoundException e) {
                    System.out.println("Transfer file error");
                    e.printStackTrace();
                    break;
                }
            }
        }
    });
}
