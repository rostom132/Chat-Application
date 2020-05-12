package com.muc;

import java.net.Socket;

public class PeerServerHandler extends Thread{

    private final Socket peerSocket ;

    public PeerServerHandler(Socket peerSocket) {
        this.peerSocket = peerSocket;
    }

    @Override
    public void run() {

    }

    public void communicate() {

    }
}
