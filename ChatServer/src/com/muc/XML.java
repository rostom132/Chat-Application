package com.muc;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class XML {

    public XML() {
    }

    public void Encoder(UserInfo userInfo) throws IOException {
        // Write new user info
        FileOutputStream os = null;
        String new_userName = userInfo.getUserName();
        try {
            os = new FileOutputStream(new File("./ChatServer/User/" + new_userName +".xml"));
            XMLEncoder encoder = new XMLEncoder(os);
            encoder.writeObject(userInfo);
            encoder.close();
            os.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

//    public void Decoder(String file, UserInfo userInfo) {
//        FileInputStream is = null;
//        try {
//            is = new FileInputStream(new File(file));
//            XMLDecoder decoder = new XMLDecoder(is);
//            userInfo = (UserInfo) decoder.readObject();
//            decoder.close();
//            is.close();
//            // Store all the xml info to the HashMap
//            userData.put(userInfo.getUserName(), userInfo);
//            System.out.println(userData);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

//    public void getUserInfoXML() {
//        // Read all user info
//        try (Stream<Path> walk = Files.walk(Paths.get("./ChatServer/User"))) {
//
//            List<String> result = walk.filter(Files::isRegularFile)
//                    .map(x -> x.toString()).collect(Collectors.toList());
//            if(result == null)  {
//                return;
//            } else {
//                for (String s : result) {
//                    Decoder(s);
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}
