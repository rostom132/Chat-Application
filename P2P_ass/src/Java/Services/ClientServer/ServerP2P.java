package Java.Services.ClientServer;

import Java.Controller.main.confirmBox ;
import Java.Controller.main.mainUIController;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerP2P {
    ServerSocket serverPeer;
    String user_name;
    private volatile mainUIController user_remote;
    private int port;
    private WaitToConnect waitingThread;

    ChangeListener<Boolean> changeServerStatus = new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
            if (!newValue) {
                try {
                    waitingThread.terminate();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    public ServerP2P(String name, mainUIController user_remote, ClientHandler current_client) throws IOException {
        user_name = name;
        System.out.println(name + " " + current_client.getOwnerInfo().getIP() + " " + current_client.getOwnerInfo().getPort());
        this.user_remote = user_remote;
        this.user_remote.currentClient = current_client;
        this.user_remote.currentClient.getState().addListener(this.user_remote.mainUIcontrol);
        this.port = this.user_remote.currentClient.getOwnerInfo().getPort();
        System.out.println(port);
        serverPeer = new ServerSocket(port);
        this.waitingThread = new WaitToConnect();
        Thread thread = new Thread(waitingThread);
        thread.start();
        this.user_remote.startController(current_client);
        this.user_remote.getServerOnlineStatus().addListener(changeServerStatus);

    }

    class WaitToConnect implements Runnable {
        private volatile boolean running = true;

        public void terminate() throws IOException {
            serverPeer.close();
            running = false;
        }

        @Override
        public void run() {
            while (running) {
                try {
                    Socket s = serverPeer.accept();
                    DataInputStream dis = new DataInputStream(s.getInputStream());
                    DataOutputStream dos = new DataOutputStream(s.getOutputStream());
                    String guest_name = dis.readUTF();
                    Thread confirm = new Thread(new Runnable()
                    {
                        @Override
                        public void run() {
                            boolean Confirm = confirmBox.checkConfirm("New chat request!", "Do you want to chat with "+ guest_name + " ?");
                            if (Confirm){
                                Platform.runLater(() -> {
                                    user_remote.newChatRoom(guest_name,s,dis,dos);
                                });
                                try {
                                    dos.writeUTF("Accept");
                                    dos.flush();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            }
                            else {
                                try {
                                    dos.writeUTF("Deny");
                                    dos.flush();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                System.out.println("Don't chat with " + guest_name + ". Exit!");
                            }
                            System.out.println("end with" + guest_name);
                        }
                    });
                    confirm.start();
                } catch (Exception e) {
                    break;
                }
            }
            try {
                serverPeer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}

