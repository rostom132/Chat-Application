package com.muc;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class Client {
    final static int ServerPort = 8818;

    private static void receiveFile(Socket socket, PrintWriter out, String fileName) throws IOException {
        byte[] fileBuffer = new byte[20000];
        InputStream is = socket.getInputStream();
        is.read(fileBuffer, 0, fileBuffer.length);

        FileOutputStream fos = new FileOutputStream("result" + fileName);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        bos.write(fileBuffer, 0, fileBuffer.length);
        bos.close();
        // Send response to server
        String response = "OK fileTransfer";
        out.println(response);
    }

    public static void main(String[] args) throws IOException {
        InetAddress ip = InetAddress.getByName("localhost");
        Socket s = new Socket(ip, ServerPort);

        BufferedReader input = new BufferedReader(new InputStreamReader(s.getInputStream()));
        BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out =  new PrintWriter(s.getOutputStream(), true);

        // sendMessage Thread
        Thread sendMessage = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        String cmd = keyboard.readLine();
                        out.println(cmd);
                        if(cmd.equalsIgnoreCase("quit") || cmd.equalsIgnoreCase("logoff")) break;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
                try {
                    s.close();
                    System.exit(0);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        Thread readMessage = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    try {
                        String serverResponse = input.readLine();
                        if(serverResponse != null) {
                            if(serverResponse.contains("sending")) {
                                System.out.println(serverResponse);
                                String[] responseArr = serverResponse.split(":", 2);
                                String fileName = responseArr[1];
                                receiveFile(s, out, fileName);
                            } else {
                                serverResponse = "Server says: " + serverResponse + "\n";
                                System.out.println(serverResponse);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        sendMessage.start();
        readMessage.start();
    }
}
