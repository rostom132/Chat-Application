package internetServer;

import Java.Services.internetServer.UserInfo;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server extends Thread{
    private final int serverPort;
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
        this.userList.add(serverHandler);
    }

    public void removeUser(ServerHandler serverHandler) {
        this.userList.remove(serverHandler);
    }

    // Methods for processing userInfo
    public HashMap<String, UserInfo> getUserInfo() {
        return userData;
    }

    public void addUserInfo(String username, UserInfo userInfo) {
        this.userData.put(username, userInfo);
    }

    // Methods for processing request
    public HashMap<String, UserInfo> getRequestPool() {
        return requestPool;
    }

    public void removeUserInfo(String user_userName) {
        Iterator<Map.Entry<String, UserInfo>> iterator = userData.entrySet().iterator();
        while(iterator.hasNext()) {
            Map.Entry<String, UserInfo> entryPoint = iterator.next();
            if(entryPoint.getKey().equals(user_userName)) iterator.remove();
        }
    }

    public int getPort(String user_name) {
        int port = 0;
        for(String s : userData.keySet()) {
            if(s.equals(user_name)) {
                port = userData.get(s).getPort();
            }
        }
        return port;
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
