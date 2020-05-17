package Java.Services.ClientServer;

import Java.Controller.main.confirmBox ;
import Java.Controller.main.mainController;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    ServerSocket serverPeer;
    String user_name;
    private volatile mainController user_remote;
    private int port = 8181;
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

    public Server(String name, mainController user_remote, int port) throws IOException {
        user_name = name;
        serverPeer = new ServerSocket(port);
        this.user_remote = user_remote;
        this.port = port;
        this.waitingThread = new WaitToConnect();
        Thread thread = new Thread(waitingThread);
        thread.start();
        this.user_remote.startController(name);
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
                    System.out.println("Conneted to " + s);
                    DataInputStream dis = new DataInputStream(s.getInputStream());
                    DataOutputStream dos = new DataOutputStream(s.getOutputStream());
                    String guest_name = dis.readUTF();
                    System.out.println("Do you want to chat with " + guest_name + "(Accept or Deny)?");
//
                    Thread confirm = new Thread(new Runnable()
                    {
                        @Override
                        public void run() {
                            boolean Confirm = confirmBox.checkConfirm(guest_name);
                            if (Confirm){
                                Platform.runLater(() -> {
                                    user_remote.newChatRoom(guest_name,s,dis,dos);
                                });
                                System.out.println("Start chatting with " + guest_name);
                                try {
                                    dos.writeUTF("Accept");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            }
                            else {
                                try {
                                    dos.writeUTF("Deny");
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
            System.out.println("end Chattttttttttttt");
            try {
                serverPeer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}

