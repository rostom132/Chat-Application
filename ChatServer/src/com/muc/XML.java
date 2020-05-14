package com.muc;

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

    private HashMap<String, UserInfo> userData = null;

    public XML() {
    }

    public XML(HashMap<String, UserInfo> userData) {
        this.userData = userData;
    }

    public void Encoder(String writtenPath, UserInfo userInfo, ArrayList<UserInfo> friendList) throws IOException {
        // Write all the information of user to xml file
        FileOutputStream os = null;
        String user_userName = userInfo.getUserName();
        try {
            os = new FileOutputStream(new File(writtenPath + "/" + user_userName + ".xml"));
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
                for (int i = 0; i < length; i++) {
                    UserInfo friendInfo = (UserInfo) decoder.readObject();
                    // Write all the userFriend in the friend list
                    friendList.add(friendInfo);
                }
            }
            is.close();
            decoder.close();
        } catch(FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void readFileUser(String userPath) {
        // Read all userInfo.xml
        try (Stream<Path> walk = Files.walk(Paths.get(userPath))) {

            List<String> result = walk.filter(Files::isRegularFile)
                    .map(x -> x.toString()).collect(Collectors.toList());
            if(result == null)  {
                return;
            } else {
                for (String s : result) {
                    Decoder(s, null, "Info");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readFileFriend(String userPath, UserInfo userInfo, ArrayList<UserInfo> friendList, String typeToRead) {
        String user_userName = userInfo.getUserName();
        try (Stream<Path> walk = Files.walk(Paths.get(userPath))) {
            List<String> result = walk.filter(Files::isRegularFile)
                    .map(x -> x.toString()).collect(Collectors.toList());
            if(result == null)  {
                return;
            } else {
                String concact = user_userName + ".xml";
                for (String s : result) {
                    System.out.println(s);
                    if(s.contains(concact)) {
                        System.out.println(s + ":" + concact);
                        Decoder(s, friendList, "Friend");
                        break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
