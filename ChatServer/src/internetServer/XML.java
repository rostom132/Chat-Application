package internetServer;

import Java.Services.User.FriendInfo;
import Java.Services.internetServer.UserInfo;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class XML {

    private String userPath = "./ChatServer/User";
    private HashMap<String, UserInfo> userData = null;

    public XML() {
    }

    public XML(HashMap<String, UserInfo> userData) {
        this.userData = userData;
    }

    public String getUserPath() {
        return userPath;
    }

    public List<String> getXMLFile() {
        List<String> result = null;
        try (Stream<Path> walk = Files.walk(Paths.get(userPath))) {

            result = walk.filter(Files::isRegularFile)
                    .map(x -> x.toString()).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void Encoder(UserInfo userInfo) throws IOException {
        // Write all the information of user to xml file
        FileOutputStream os = null;
        String user_userName = userInfo.getUserName();
        int list_friend_size = userInfo.getFriendList().size();
        System.out.println(user_userName);
        try {
            os = new FileOutputStream(new File(userPath + "/" + user_userName + ".xml"));
            XMLEncoder encoder = new XMLEncoder(os);
            // Write the userInfo
            encoder.writeObject(userInfo);
            encoder.close();
            os.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void Decoder(String file, ArrayList<FriendInfo> friendList, String typeToRead) throws IOException {
        FileInputStream is = null;
        try {
            is = new FileInputStream(new File(file));
            XMLDecoder decoder = new XMLDecoder(is);
            // Read the userInfo
            UserInfo userInfo = (UserInfo) decoder.readObject();
            int friend_list_length =  userInfo.getFriendList().size();
            System.out.println("XML Notification: " + friend_list_length);
            // Write all the userInfo to the hashmap
            if(typeToRead.equals("Info")) {
                UserInfo user_hashmap = new UserInfo();
                user_hashmap.setUserName(userInfo.getUserName());
                user_hashmap.setPassWord(userInfo.getPassWord());
                user_hashmap.setPort(userInfo.getPort());
                userData.put(userInfo.getUserName(), user_hashmap);
            } else {
                // Get the length of the friend list
                if(friend_list_length > 0) {
                    for (FriendInfo friend : userInfo.getFriendList()) {
                        // Write all the userFriend in the friend list
                        System.out.println("XML Notification: " + friend.getFriendName()+ " " + friendList.size());
                        friendList.add(friend);
                        System.out.println("After add:" + friendList.size());
                    }
                }
            }
            is.close();
            decoder.close();
        } catch(FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void readFileUser(List<String> XMLFile) throws IOException {
        if(XMLFile == null) return;
        for (String s : XMLFile) {
            Decoder(s, null, "Info");
        }
    }

    public void readFileFriend(List<String> XMLFile, UserInfo userInfo, ArrayList<FriendInfo> friendList) throws IOException {
        if(XMLFile == null) return;
        String user_userName = userInfo.getUserName();
        String concact = user_userName + ".xml";
        for (String s : XMLFile) {
            if(s.contains(concact)) {
                System.out.println(s + " : " + concact);
                // Case Read: "Friend" and "Info"
                Decoder(s, friendList, "Friend");
                break;
            }
        }
    }

    public void interactFriendContainer(List<String> XMLFile, UserInfo userInfo, String removeName, boolean status, String interact_type) throws IOException {
        if(XMLFile == null) return;
        String user_userName = userInfo.getUserName();
        String concact = user_userName + ".xml";
        for (String s : XMLFile) {
            if(s.contains(concact)) {
                System.out.println(s);
                interactFriend(s, removeName, status, interact_type);
                break;
            }
        }
    }

    public void removeUser(List<String> XMLFile, UserInfo userInfo) {
        // Remove the userInfo from the database
        if(XMLFile == null) return;
        String user_userName = userInfo.getUserName();
        String concact = user_userName + ".xml";
        for(String s : XMLFile) {
            if(s.contains(concact)) {
                File f = new File(s);
                    if(f.delete()) {
                        System.out.println("Deleted successfully");
                    }
            }
        }
    }

    public void interactFriend(String file, String removeName, boolean status, String interact_type) throws IOException {
        System.out.println("Begin XML operations");
        FileInputStream is = null;
        try {
            is = new FileInputStream(new File(file));
            XMLDecoder decoder = new XMLDecoder(is);
            // Read the userInfo
            UserInfo userInfo = (UserInfo) decoder.readObject();
            // Create new friendList array to store
            ArrayList<FriendInfo> new_friend_list = new ArrayList<FriendInfo>();
            System.out.println("Temp before: ");
            for(FriendInfo friend : userInfo.getFriendList()) {
                if(!friend.getFriendName().equals(removeName)) {
                    new_friend_list.add(friend);
                }
                else if(friend.getFriendName().equals(removeName)) {
                    if(interact_type.equals("Update")) {
                        friend.setStatus(status);
                        new_friend_list.add(friend);
                    }
                }
            }
            userInfo.setFriendList(new_friend_list);
            System.out.println("Temp array after: " + interact_type);
            for(FriendInfo friend : new_friend_list) {
                System.out.print(friend.getFriendName() + ":" + friend.getStatus() + ":" + friend.getFriendIP() + ":" + friend.getPort());
                System.out.println(" + ");
            }
            System.out.print("\n");
            Encoder(userInfo);
            is.close();
            decoder.close();
        } catch(FileNotFoundException e) {
            e.printStackTrace();
        }
        return;
    }

}
