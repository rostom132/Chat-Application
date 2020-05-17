package internetServer;

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

    Thread sendMessage = new Thread(new Runnable() {
        @Override
        public void run() {
            while (true) {
                try {
                    String cmd = keyboard.nextLine();
                    dos.writeUTF(cmd);
                    if(cmd.contains("quit")) break;
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
                            case "friend":

                                break;
                            case "sending":
                                String fileName = tokens[1];
                                System.out.println("FileName: " + fileName);
                                receiveFile(fileName);
                                break;
                            case "end":
                                endConnection = true;
                                break;
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Transfer file error");
                    e.printStackTrace();
                }
            }
            System.exit(0);
        }
    });
}
