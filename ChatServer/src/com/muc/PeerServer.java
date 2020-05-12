package com.muc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class PeerServer extends Thread{
    private int port;
    private OutputStream serverOut;
    private InputStream serverIn;


    public PeerServer(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try {
            ServerSocket peerServer = new ServerSocket(port);
            while(true) {
                Socket peerSocket = peerServer.accept();
                PeerServerHandler peer = new PeerServerHandler(peerSocket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
