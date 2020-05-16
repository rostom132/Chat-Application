package internetServer;

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

    public void Encoder(UserInfo userInfo, ArrayList<UserInfo> friendList) throws IOException {
        // Write all the information of user to xml file
        FileOutputStream os = null;
        String user_userName = userInfo.getUserName();
        try {
            os = new FileOutputStream(new File(userPath + "/" + user_userName + ".xml"));
            XMLEncoder encoder = new XMLEncoder(os);
            // Write the userInfo
            encoder.writeObject(userInfo);
            // Write the length of the friend list
            Integer numOfFriend = friendList.size();
            encoder.writeObject(numOfFriend);
            // Write the info of each friend
            for (UserInfo user : friendList) {
                encoder.writeObject(user);
            }
            encoder.close();
            os.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void Decoder(String file, ArrayList<UserInfo> friendList, String typeToRead) throws IOException {
        FileInputStream is = null;
        try {
            is = new FileInputStream(new File(file));
            XMLDecoder decoder = new XMLDecoder(is);
            // Read the userInfo
            UserInfo userInfo = (UserInfo) decoder.readObject();
            // Write all the userInfo to the hashmap
            if(typeToRead.equals("Info")) {
                userData.put(userInfo.getUserName(), userInfo);
            } else {
                // Get the length of the friend list
                Integer length = (Integer) decoder.readObject();
                if(length != null) {
                    for (int i = 0; i < length; i++) {
                        UserInfo friendInfo = (UserInfo) decoder.readObject();
                        // Write all the userFriend in the friend list
                        friendList.add(friendInfo);
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

    public void readFileFriend(List<String> XMLFile, UserInfo userInfo, ArrayList<UserInfo> friendList) throws IOException {
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

    public void removeFileContainer(List<String> XMLFile, UserInfo userInfo, String removeName) throws IOException {
        if(XMLFile == null) return;
        String user_userName = userInfo.getUserName();
        String concact = user_userName + ".xml";
        for (String s : XMLFile) {
            if(s.contains(concact)) {
                System.out.println(s);
                removeFriend(s, removeName);
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

    public void removeFriend(String file, String removeName) throws IOException {
        FileInputStream is = null;
        try {
            is = new FileInputStream(new File(file));
            XMLDecoder decoder = new XMLDecoder(is);
            // Pass the read the userInfo
            UserInfo userInfo = (UserInfo) decoder.readObject();
            // Read of the length of friend list
            Integer length = (Integer) decoder.readObject();
            // Create new friendList array to store
            ArrayList<UserInfo> temp = new ArrayList<UserInfo>();
            System.out.println("Temp before: ");
            for (int i = 0; i < length; i++) {
                UserInfo friendInfo = (UserInfo) decoder.readObject();
                if(!friendInfo.getUserName().equals(removeName)) {
                    temp.add(friendInfo);
                }
            }
            System.out.println("Temp after: " + temp);
            for(UserInfo user : temp) {
                System.out.println(user.getUserName());
            }
            Encoder(userInfo, temp);
            is.close();
            decoder.close();
        } catch(FileNotFoundException e) {
            e.printStackTrace();
        }
        return;
    }

}
