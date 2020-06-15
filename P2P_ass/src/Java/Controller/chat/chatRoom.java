package Java.Controller.chat;

import Java.Controller.main.confirmBox;
import Java.Controller.main.notiBox;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.locks.ReentrantLock;

import static Java.Controller.chat.messDirection.SEND;
import static Java.Controller.chat.messDirection.RECEIVE;
//import static Java.Controller.main.notiBox.displayNoti;


public class chatRoom {
    private String user_name;
    private String guest_name;
    private String guest_ip;
    private boolean chatting;
    private int port;
    private Chat currentChat;
    private Request currentRequest;

    private ObservableList<message> list = FXCollections.observableArrayList();
    public ListView<message> messList = new ListView<message>();
    public Button requestChat = new Button();
    public Label rec_file = new Label("Receiving File");

    private IntegerProperty number_unseen_mess = new SimpleIntegerProperty(this, "number_unseen_mess", 0 );
    private IntegerProperty chat_status = new SimpleIntegerProperty(0);
    private BooleanProperty online = new SimpleBooleanProperty(false);

    private ReentrantLock mutex = new ReentrantLock();

    public String getGuestName(){
        return guest_name;
    }

    public Chat getCurrentChat() {
        return currentChat;
    }

    public IntegerProperty getChatAccept() {
        return chat_status;
    }

    public IntegerProperty getNumUnseenMess(){
        return number_unseen_mess;
    }

    public int getPort(){return this.port;}


    public String getIP(){return this.guest_ip;}

    public void setChatting(boolean chatting){
        this.chatting = chatting;
    }

    public BooleanProperty onlineProperty() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online.set(online);
    }

    public BooleanProperty getOnline() {
        return this.online;
    }

    public void setGuestIP(String ip){
        this.guest_ip = ip;
    }
//    public void setUserName(String name){ this.user_name = name;}x

    public void offRoom() throws IOException {
        currentChat.terminateChat();
        this.chatting = false;
        this.chat_status.set(0);
        Platform.runLater(() -> {
            this.requestChat.setText("Request");
            this.requestChat.setOnAction(requestChatting);
        });
    }

    EventHandler<ActionEvent> requestChatting = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            if (chatRoom.this.getChatAccept().get()==0) {
                currentRequest = new Request();
                requestChat.setText("Pending...");
                chatRoom.this.requestChat.setOnAction(null);
                System.out.println(port);
            }
        }
    };

    public void chatError(String status){
        Platform.runLater(() -> {
            this.requestChat.setText("Request");
            this.requestChat.setOnAction(requestChatting);
        });
        if (status.equals(("Denied!"))) {
            notiBox.displayNoti("Deny","Request denied from " + guest_name);
        }else if (status.equals("offline")){
            notiBox.displayNoti("Notification" ,guest_name + " is offline!");
        }else {
            notiBox.displayNoti("Cannot connet",guest_name + " is not Online!");
        }
    }

    public chatRoom(String user_name,String guest_name, String ip,int port, boolean status){
        requestChat.setText("Request");
        requestChat.setStyle("-fx-background-color: #4CAF50;-fx-text-fill: aliceblue;  -fx-text_alignment: center; -fx-pref-height: 80px; -fx-pref-width: 120px");
        this.requestChat.setOnAction(requestChatting);
        this.rec_file.setText("Receiving File from " + guest_name);
        this.rec_file.setStyle("-fx-background-color: #508EE6;-fx-text-fill: aliceblue;  -fx-text_alignment: center; -fx-pref-height: 80px; -fx-pref-width: 200px");
        this.guest_ip = ip;
        this.user_name = user_name;
        this.guest_name = guest_name;
        this.port = port;
        this.chatting = false;
        this.online.set(status);
    }

    public void getStartChat(Socket connection, DataInputStream data_in, DataOutputStream data_out){
        messList.setCellFactory(param -> new Cell());
        messList.setItems(list);
        this.currentChat = new Chat(connection,data_in,data_out);
        list.add(new message(user_name,SEND,"New chat! Hello " + guest_name));
        this.chat_status.set(1);
    }

    public void sendMess(String newmess) {
        if (chat_status.get()==1 && chatting && !newmess.equals("")) {
            currentChat.sendMessage(newmess);
            list.add(new message(user_name, SEND, newmess));
            messList.scrollTo(list.size() - 1);
        }
    }

    public void recMess(String newmess){
        if (newmess == "") return;
        if(newmess.equals("send_file") && chat_status.get()==1){
            Thread temp = new Thread() {
                public void run() {
                    try {
                        System.out.println("nhan file nhan file");
                        currentChat.receiveFile("new_file_from_"+guest_name);
                    } catch (IOException  e) {
                        e.printStackTrace();
                    }
                }
            };
            temp.start();
            return;
        }
        list.add(new message(guest_name,RECEIVE, newmess));
        messList.scrollTo(list.size()-1);
        if (!chatting) {
            this.number_unseen_mess.set(number_unseen_mess.get() + 1);
        }
    }


    public void resetUnseenMess(){
        number_unseen_mess.set(0);
    }



    public class Chat {
        private Socket connection;
        private DataInputStream data_in;
        private DataOutputStream data_out;
        private volatile boolean working = false;
        private volatile boolean sending_file = false;
        private Thread rec_thread;
        private Object lock = new Object();
        private volatile String sending_file_mes = null;

        public Chat(Socket connection, DataInputStream data_in, DataOutputStream data_out) {
            this.connection = connection;
            this.data_in = data_in;
            this.data_out = data_out;
//            (new Thread(new sendMessage())).start();

            rec_thread = new Thread(new recMessage());
            rec_thread.start();
        }


        public void terminateChat() throws IOException {
            this.connection.close();
            this.working = false;
        }

        public void transferFile(File my_file) throws IOException {
            sending_file = true;
            chat_status.set(2);
            rec_file.setText("Sending file...");
            boolean send_accept = false;
            if (!my_file.exists() || !my_file.isFile()) return;
            System.out.println("send file");
            int fileSize = (int) my_file.length();
            byte b[] = new byte[fileSize];
            FileInputStream fis =  new FileInputStream(my_file);
            BufferedInputStream bis = new BufferedInputStream(fis);

            data_out.writeUTF("send_file " + my_file.getName());
            data_out.writeUTF(Integer.toString(fileSize));
            System.out.println(Integer.toString(fileSize));
            // Wait for confirmation
            String msg_rec = null;
            while (sending_file_mes == null){}
            if (sending_file_mes.equals("OK_send_file")) {
                System.out.println("OK check file size is done");
                bis.read(b, 0, b.length);
                OutputStream os = null;
                os = connection.getOutputStream();
                os.write(b, 0, b.length);
                System.out.println("Done send");
                os.flush();
                send_accept = true;
            } else if (sending_file_mes.equals("No")) {
                notiBox.displayNoti("Can not transfer file!", guest_name + " denied!");
                send_accept = false;
            }
            sending_file_mes = null;
            while (send_accept) {
                if (data_in.readUTF().equals("Done_sendfile")) break;
            }
            chat_status.set(1);
            sending_file = false;
            synchronized(lock) {
                lock.notify();
            }
        }

        public void receiveFile(String fileName) throws IOException {
            chat_status.set(2);
            rec_file.setText("Receiving File...");
            int fileSize = Integer.parseInt(data_in.readUTF());
            System.out.println(fileSize);
            if(fileSize > 0){
                if (!confirmBox.checkConfirm("Send File Request!","Do you want to receive " +fileName + " from " + guest_name +"?")){
                    data_out.writeUTF("Request is denied");
                    data_out.writeUTF("No");
                    sending_file = false;
                    synchronized (lock){
                        lock.notify();
                    }
                    return;
                }
                data_out.flush();
//                data_out.writeUTF("send");
//                data_out.flush();
                data_out.writeUTF("OK_send_file");
                data_out.flush();
                System.out.println("check size ok");
                byte[] b = new byte[fileSize];
                InputStream is = connection.getInputStream();

                File file = new File(guest_name+ "_" +  fileName);
                FileOutputStream fos = new FileOutputStream(file);
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                int byteRead;
                while ((byteRead = is.read())!= -1) {
                    bos.write(byteRead);
                    fileSize--;
                    if(fileSize == 0) break;
                }
                System.out.println("send file done");
                bos.flush();
                bos.close();
                fos.close();
                data_out.writeUTF("Done_sendfile");
            }
            sending_file = false;
            chat_status.set(1);
            synchronized (lock){
                lock.notify();
            }
        }


        public void sendMessage(String mess) {
            try {
                data_out.writeUTF(mess);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public class recMessage implements Runnable {
            @Override
            public void run() {
                synchronized (lock){
                    while (true) {
                        try {
                            // read the message sent to this client
                            while(sending_file) {
                                lock.wait();
                                System.out.println( "bay mau");
                            }
                            String message = data_in.readUTF();
                            if(sending_file) sending_file_mes = message;
                            System.out.println("new"+message+"new");
                            String arr_mess[] =  message.split(" ", 2);
                            if (arr_mess[0].equals("send_file")) {
                                receiveFile(arr_mess[1]);
                                sending_file = true;
                                continue;
                            }
                            Platform.runLater(() -> {
                                chatRoom.this.recMess(message);
                            });
                        } catch (IOException | InterruptedException e) {
                            try {
                                connection.close();
                                chatRoom.this.offRoom();
                            } catch (IOException ioException) {
                                ioException.printStackTrace();
                            }
                            break;
                        }
                    }
                }
            }
        }
    }

    public class Request {

        public Request(){
            (new Client()).start();
        }
        class Client extends Thread {
            @Override
            public void run() {

                Socket s = null;
                DataInputStream dis = null;
                DataOutputStream dos = null;
                try {
                    System.out.println(chatRoom.this.getIP());
                    s = new Socket(chatRoom.this.getIP(), chatRoom.this.getPort());
                    dis = new DataInputStream(s.getInputStream());
                    dos = new DataOutputStream(s.getOutputStream());
                    dos.writeUTF(user_name);
                } catch (IOException e) {
                    chatError("Can't Connet!");
                    return;
                }
                while(true){
                    try {
                        String read_mess = dis.readUTF();
                        if (read_mess.equals("Accept")){
                            chatRoom.this.getStartChat(s,dis,dos);
                            break;
                        }
                        else if (read_mess.equals("Deny")){
                            System.out.println("Denied");
                            chatError("Denied!");
                            break;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    static class Cell extends ListCell<message> {
        HBox box_send = new HBox();
        Label sender_send = new Label();
        Label mess_send = new Label();
        //        BorderPane container = new BorderPane();

        HBox box_receive = new HBox();
        Label sender_rec = new Label();
        Label mess_rec = new Label();

        Pane blank = new Pane();
        public Cell(){
            super();
//            number.getChildren().addAll(online_icon_pallet,receive_mess);
            //design box_send
//            content.setText(mess.getSender());
            mess_send.setWrapText(true);
            mess_send.setMaxWidth(220);
            mess_send.setStyle("-fx-background-color: #beb9b9;-fx-background-radius: 15");
            mess_send.setPadding(new Insets(5,8,5,8));
            sender_send.setFont(Font.font("Cambaria", FontWeight.BOLD, 16));
            box_send.getChildren().addAll(sender_send,mess_send,blank);
            box_send.setAlignment(Pos.CENTER_LEFT);
            box_send.setHgrow(blank, Priority.ALWAYS);

            //design box_rec
            mess_rec.setWrapText(true);
            mess_rec.setMaxWidth(220);
            mess_rec.setStyle("-fx-background-color: #4CAF50;-fx-background-radius: 15");
            sender_rec.setFont(Font.font("Cambaria", FontWeight.BOLD, 16));
            mess_rec.setPadding(new Insets(5,8,5,8));
            box_receive.getChildren().addAll(blank,mess_rec,sender_rec);
            box_receive.setAlignment(Pos.CENTER_RIGHT);
            box_receive.setHgrow(blank, Priority.ALWAYS);
        }
        public void updateItem(message mess, boolean empty){
            super.updateItem(mess,empty);
            setText(null);
            setGraphic(null);

            if(mess!= null && !empty){
                if (mess.messDirect() == SEND){
                    sender_send.setText(mess.getSender()+"  ");
                    mess_send.setText(mess.getMess());
                    setGraphic(box_send);
                } else {
                    sender_rec.setText("  "+mess.getSender());
                    mess_rec.setText(mess.getMess());
                    setGraphic(box_receive);
                }
            }
        }
    }

}
