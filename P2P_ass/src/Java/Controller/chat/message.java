package Java.Controller.chat;

enum messDirection {
    SEND,
    RECEIVE
}

public class message {
    private String sender;
    private messDirection mess_direction;
    private String mess;

    public message(String sender, messDirection send_message, String mess){
        this.sender = sender;
        this.mess_direction = send_message;
        this.mess = mess;
    }

    public String getSender() {
        return sender;
    }

    public messDirection messDirect() {
        return this.mess_direction;
    }
    public String getMess(){
        return this.mess;
    }
}

