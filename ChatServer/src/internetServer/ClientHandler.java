package internetServer;

import Java.Services.User.FriendInfo;
import Java.Services.internetServer.UserInfo;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class ClientHandler {

    private final InetAddress ip;
    private final int serverPort;
    private Socket s;

    private DataInputStream dis;
    private DataOutputStream dos;
    private Scanner keyboard;

    private ArrayList<FriendInfo> friend_List = new ArrayList<FriendInfo>();
    private UserInfo user_info;

    private int numOfRequest = 0;

    private boolean endConnection = false;

    public ClientHandler(InetAddress ip, int serverPort) {
        this.ip = ip;
        this.serverPort = serverPort;
        clientWorking();
    }

    public void clientWorking() {
        try {
            s = new Socket(ip, serverPort);
            keyboard = new Scanner(System.in);
            dis = new DataInputStream(s.getInputStream());
            dos = new DataOutputStream(s.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        sendMessage.start();
        readMessage.start();
    }

    private void receiveFriendList() throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
        user_info = (UserInfo) ois.readObject();
        friend_List = user_info.getFriendList();
        System.out.println("Friend list size: " + friend_List.size());
        System.out.println(user_info.getPort());
    }

    private void receiveFriendInfo(String friend_name, String statusStr, String friend_ip, int port){
        boolean status;
        if(statusStr.equals("true")) status = true;
        else status = false;
        FriendInfo newFriend = new FriendInfo(friend_name, status, friend_ip, port);
        addFriend(newFriend);
    }

    private void receiveFile(String fileName) throws IOException {
        int fileSize;
        fileSize = Integer.parseInt(dis.readUTF());
        System.out.println(fileSize);
        if(fileSize > 0){
            dos.writeUTF("OK");
            byte[] b = new byte[fileSize];
            InputStream is = s.getInputStream();

            File file = new File("result" +  fileName);
            FileOutputStream fos = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            int byteRead;
            while ((byteRead = is.read())!= -1) {
                bos.write(byteRead);
                fileSize--;
                if(fileSize == 0) break;
            }
            bos.flush();
            bos.close();
            fos.close();
            System.out.println("Done");
            dos.writeUTF("Done");
        }
    }

    private void addFriend(FriendInfo FI){
        this.friend_List.add(FI);
    }

    private void removeByName(String name){
        for(int i = 0; i < friend_List.size(); i++){
            if(friend_List.get(i).getFriendName().equals(name)){
                friend_List.remove(i);
                break;
            }
        }
    }
    private void displayFriendList(){
        System.out.println(friend_List.size());
        for(int i = 0; i < friend_List.size(); i++){
            System.out.println(friend_List.get(i).getFriendName()+" "+friend_List.get(i).getStatus()+" "+ friend_List.get(i).getFriendIP() + " "  + friend_List.get(i).getPort());
        }
    }

    private void updateFriendStatus(String name, boolean status){
        int index = searchByName(name);
        friend_List.get(index).setStatus(status);
    }

    private void updateFriendIP(String name, String IP){
        int index = searchByName(name);
        friend_List.get(index).setFriendIP(IP);
    }

    private int searchByName(String name){
        int index = 0;
        for(int i = 0; i < friend_List.size(); i++){
            if(friend_List.get(i).getFriendName().equals(name)){
                index = i;
                break;
            }
        }
        return index;
    }

    Thread sendMessage = new Thread(new Runnable() {
        @Override
        public void run() {
            while (true) {
                try {
                    String cmd = keyboard.nextLine();
                    String[] tokens = StringUtils.split(cmd);
                    if(tokens.length > 0 && tokens != null) {
                        String cmd_key = tokens[0];
                        switch (cmd_key) {
                            case "search":
                                String searchName = tokens[1];
                                searchByName(searchName);
                                break;
                            case "view":
                                // Function in the UI
                                System.out.println("Trigger the handlePoolRequest");
                                dos.writeUTF(cmd);
                                break;
                            case "sendfile":
                                // cmd: sendfile filename receiver_name
                                System.out.println("Sending sendfile signal");
                                dos.writeUTF(cmd);
                                break;
                            case "friend": // View the friend list
                                displayFriendList();
                                break;
                            case "quit":
                                System.out.println("Quitting");
                                dos.writeUTF(cmd);
                                return;
                            default:
                                dos.writeUTF(cmd);
                                break;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    });

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
                                receiveFriendList();
                                break;

                            case "remove":
                                String remove_name = tokens[1];
                                removeByName(remove_name);
                                break;

                            case "status":
                                String status_name = tokens[1];
                                String status_str = tokens[2];
                                boolean status;
                                if(status_str.equals("true")) status = true;
                                else status = false;
                                updateFriendStatus(status_name, status);
                                break;

                            case "ip":
                                String ip_name = tokens[1];
                                String ip_address = tokens[2];
                                updateFriendIP(ip_name, ip_address);
                                break;

                            case "new_request":
                                numOfRequest++;
                                System.out.println("Request count: " + numOfRequest);
                                break;

                            case "request_add":
                                // Trigger yes/no noti in UI
                                String request_add_user = tokens[1];
                                System.out.println(request_add_user + " wants to add you");
                                break;

                            case "request_send":
                                // Trigger yes/no in UI
                                String request_send_user = tokens[1];
                                String file_name = tokens[2];
                                System.out.println(request_send_user + " wants to send you a file name " + file_name);
                                break;

                            case "sending":
                                String fileName = tokens[1];
                                System.out.println("FileName: " + fileName);
                                receiveFile(fileName);
                                break;

                            case "add":
                                String friend_name = tokens[1];
                                String statusStr = tokens[2];
                                String friend_ip = tokens[3];
                                String port = tokens[4];
                                System.out.println(friend_name + ":" + statusStr + ":" + friend_ip + ":" + port);
                                receiveFriendInfo(friend_name, statusStr, friend_ip, Integer.parseInt(port));
                                break;

                            case "end":
                                endConnection = true;
                                break;
                        }
                    }
                } catch (IOException | ClassNotFoundException e) {
                    System.out.println("Transfer file error");
                    e.printStackTrace();
                }
            }
            System.exit(0);
        }
    });
}
