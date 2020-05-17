import java.io.*;
import java.net.*;
import java.util.*;

public class sender{
  static ArrayList<packet> list = new ArrayList<packet>();
  public sender(){}
  public static void main(String[] args) throws Exception {
        ServerSocket ss = new ServerSocket(8080);
        System.out.println("System is online now");
        Socket s = ss.accept();
        System.out.println(s);
        sender send = new sender();
        ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
        ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
        packet Packet = new packet("Tien", true, "192.168.1.1");
        packet Packet1 =  new packet("Thien", true, "192.168.1.1");
        list.add(Packet);
        list.add(Packet1);
        oos.writeObject(list);
        System.out.println("Done");
        ss.close();
    }
}
