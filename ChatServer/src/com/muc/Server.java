package com.muc;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.beans.XMLEncoder;
import java.beans.XMLDecoder;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Server extends  Thread{
    private final int serverPort;
    private int poolID = 0;
    private  static ExecutorService pool = Executors.newFixedThreadPool(4);

    private ArrayList<ClientHandler> userList = new ArrayList<ClientHandler>();
    private HashMap<String, UserInfo> userData = new HashMap<String, UserInfo>();
    private HashMap<String, UserInfo> requestPool = new HashMap<String, UserInfo>();

    public void Encoder(String username, String pass, int ip) throws IOException {
        // Write
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(new File("./ChatServer/User/client" + username +".xml"));
            XMLEncoder encoder = new XMLEncoder(os);

            UserInfo userInfo = new UserInfo();
            userInfo.setUserName(username);
            userInfo.setPassWord(pass);
            userInfo.setPort(ip);
            encoder.writeObject(userInfo);
            //userData.put(username, userInfo);
            encoder.close();
            os.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    public void Decoder(String file) {
        UserInfo userInfo = new UserInfo();
        FileInputStream is = null;
        try {
            is = new FileInputStream(new File(file));
            XMLDecoder decoder = new XMLDecoder(is);
            userInfo = (UserInfo) decoder.readObject();
            is.close();
            decoder.close();
            userData.put(userInfo.getUserName(), userInfo);
            System.out.println(userData);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readFile() {
        // Read
        try (Stream<Path> walk = Files.walk(Paths.get("./ChatServer/User"))) {

            List<String> result = walk.filter(Files::isRegularFile)
                    .map(x -> x.toString()).collect(Collectors.toList());
            if(result == null)  {
                return;
            } else {
                for (String s : result) {
                    Decoder(s);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public Server(int serverPort) {
        this.serverPort = serverPort;
    }

    // Methods for processing userList in server
    public ArrayList<ClientHandler> getUserList() {
        return userList;
    }

    public void addUser(ClientHandler clientHandler) {
        userList.add(clientHandler);
    }

    public void removeUser(ClientHandler clientHandler) {
        userList.remove(clientHandler);
    }

    // Methods for processing userInfo
    public HashMap<String, UserInfo> getUserInfo() {
        return userData;
    }

    public void addUserInfo(String username, UserInfo userInfo) {
        userData.put(username, userInfo);
    }

    // Methods for processing request
    public HashMap<String, UserInfo> getRequestPool() {
        return requestPool;
    }

    public int getPoolID() {
        return poolID;
    }

    public void setPoolID(int poolID) {
        this.poolID = poolID;
    }

    public void addRequest(String sender, UserInfo reveiver) {
        requestPool.put(sender, reveiver);
    }

    public void removeRequest(UserInfo receiver) {
        Iterator<Map.Entry<String, UserInfo>> iterator = requestPool.entrySet().iterator();
        while(iterator.hasNext()) {
            Map.Entry<String, UserInfo> entryPoint = iterator.next();
            System.out.println(entryPoint.getKey() + " " + entryPoint.getValue().getUserName());
            if(entryPoint.getValue().getUserName().equals(receiver.getUserName())) iterator.remove();
        }
    }

     @Override
    public void run() {
        try  {
            ServerSocket serverSocket = new ServerSocket(serverPort);

            readFile();

            while(true) {
                System.out.println("About to accept client connection...");
                Socket clientSocket = serverSocket.accept();

                System.out.println("Accepted connection from" + clientSocket);
                ClientHandler newClient = new ClientHandler(this, clientSocket);
                //clientList.add(newClient);
                newClient.start();
                //pool.execute(newClient);
            }
        }  catch(IOException e) {
            e.printStackTrace();
        }
    }

}
