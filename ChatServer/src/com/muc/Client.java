package com.muc;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class Client {
    final static int ServerPort = 8818;
    private static Integer flag_count = 0;

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
                            serverResponse = "Server says: " + serverResponse + "\n";
                            System.out.println(serverResponse);
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
