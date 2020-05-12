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
    private  static ExecutorService pool = Executors.newFixedThreadPool(4);

    private ArrayList<ServerlHandler> clientList = new ArrayList<ServerlHandler>();
    private HashMap<String, UserInfo> clientData = new HashMap<String, UserInfo>();
    private HashMap<String, String> requestPool = new HashMap<String, String>();

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
            clientData.put(username, userInfo);
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
            clientData.put(userInfo.getUserName(), userInfo);
            System.out.println(clientData);
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

           for(String s:result) {
               Decoder(s);
           }

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public Server(int serverPort) {
        this.serverPort = serverPort;
    }

    public List<ServerlHandler> getClientList() {
        return clientList;
    }

    public HashMap<String, UserInfo> getClientInfo() {
        return clientData;
    }

    public HashMap<String, String> getRequestPool() {
        return requestPool;
    }

    public void addClient(ServerlHandler serverlHandler) {
        clientList.add(serverlHandler);
    }

    public void removeClient(ServerlHandler serverlHandler) {
        clientList.remove(serverlHandler);
    }

    public void addClientInfo(String username, UserInfo userInfo) {
        clientData.put(username, userInfo);
    }

    public void addRequest(String sender, String reveiver) {
        requestPool.put(sender, reveiver);
    }

    public void removeRequest(String receiver) {
        Iterator<Map.Entry<String, String>> iterator = requestPool.entrySet().iterator();
        while(iterator.hasNext()) {
            Map.Entry<String, String> entryPoint = iterator.next();
            System.out.println(entryPoint.getKey() + " " + entryPoint.getValue());
            if(entryPoint.getValue().equals(receiver)) iterator.remove();
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
                ServerlHandler newClient = new ServerlHandler(this, clientSocket);
                //clientList.add(newClient);
                newClient.start();
                //pool.execute(newClient);
            }
        }  catch(IOException e) {
            e.printStackTrace();
        }
    }

}
