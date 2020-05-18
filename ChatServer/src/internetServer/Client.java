package internetServer;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

public class Client{

    private final InetAddress ip;
    private final int serverPort;
    private Socket s;

    private BufferedReader input;
    private BufferedReader keyboard;
    private PrintWriter out;

    private ArrayList<UserInfo> friend_List = new ArrayList<UserInfo>();

    private boolean endConnection = false;

    public Client(InetAddress ip, int serverPort) {
        this.ip = ip;
        this.serverPort = serverPort;
        clientWorking();
    }

    public void clientWorking() {
        try {
            s = new Socket(ip, serverPort);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            input = new BufferedReader(new InputStreamReader(s.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        keyboard = new BufferedReader(new InputStreamReader(System.in));
        try {
            out =  new PrintWriter(s.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        sendMessage.start();
        readMessage.start();
    }

    private void receiveFile(String fileName) throws IOException {
        int fileSize;
        DataInputStream dis = new DataInputStream(s.getInputStream());
        DataOutputStream dos = new DataOutputStream(s.getOutputStream());
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
                System.out.println(byteRead);
                bos.write(byteRead);
                System.out.println("a " + fileSize);
                fileSize--;
                if(fileSize == 0) break;
            }
            System.out.println("Loop out");
            //bos.write(b, 0, b.length);
            bos.flush();
            bos.close();
            fos.close();
            System.out.println("Done");
            dos.writeUTF("Done");
        }
    }

    // sendMessage Thread
    Thread sendMessage = new Thread(new Runnable() {
        @Override
        public void run() {
            while (true) {
                try {
                    System.out.println("Dang gui");
                    String cmd = keyboard.readLine();
                    out.println(cmd);
                    if(cmd.equalsIgnoreCase("quit")) break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    });

    Thread readMessage = new Thread(new Runnable() {
        @Override
        public void run() {
            while(endConnection == false) {
                try {
                    String serverResponse = "";
                    if((serverResponse = input.readLine()) != null)  {
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
            try {
                input.close();
                out.close();
                s.close();
                System.exit(0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    });
}
