package internetServer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server extends Thread{
    private final int serverPort;
    private int poolID = 0;
    private  static ExecutorService pool = Executors.newFixedThreadPool(4);

    private List<String> XMLFile = null;
    private ArrayList<ServerHandler> userList = new ArrayList<ServerHandler>();
    private HashMap<String, UserInfo> userData = new HashMap<String, UserInfo>();
    private HashMap<String, UserInfo> requestPool = new HashMap<String, UserInfo>();

    private XML xml = new XML(userData);

    public List<String> getXMLFile() {
        return XMLFile;
    }

    public Server(int serverPort) {
        this.serverPort = serverPort;
    }

    // Methods for processing userList in server
    public ArrayList<ServerHandler> getUserList() {
        return userList;
    }

    public void addUser(ServerHandler serverHandler) {
        userList.add(serverHandler);
    }

    public void removeUser(ServerHandler serverHandler) {
        userList.remove(serverHandler);
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

    public void removeUserInfo(String user_userName) {
        Iterator<Map.Entry<String, UserInfo>> iterator = userData.entrySet().iterator();
        while(iterator.hasNext()) {
            Map.Entry<String, UserInfo> entryPoint = iterator.next();
            System.out.println(entryPoint.getKey() + " " + entryPoint.getValue().getUserName());
            if(entryPoint.getKey().equals(user_userName)) iterator.remove();
        }
    }

    public void removeRequest(UserInfo receiver) {
        Iterator<Map.Entry<String, UserInfo>> iterator = requestPool.entrySet().iterator();
        while(iterator.hasNext()) {
            Map.Entry<String, UserInfo> entryPoint = iterator.next();
            //System.out.println(entryPoint.getKey() + " " + entryPoint.getValue().getUserName());
            if(entryPoint.getValue().getUserName().equals(receiver.getUserName())) iterator.remove();
        }
    }

     @Override
    public void run() {
        try  {
            ServerSocket serverSocket = new ServerSocket(serverPort);
            XMLFile = xml.getXMLFile();
            System.out.println("File read by server: " + XMLFile);
            xml.readFileUser(XMLFile);
            while(true) {
                System.out.println("About to accept client connection...");
                Socket clientSocket = serverSocket.accept();
                if(clientSocket != null) {
                    XMLFile = xml.getXMLFile();
                    System.out.println("Accepted connection from" + clientSocket);
                    ServerHandler newClient = new ServerHandler(this, clientSocket);
                    newClient.start();
                }
                //pool.execute(newClient);
            }
        }  catch(IOException e) {
            e.printStackTrace();
        }
    }

}
