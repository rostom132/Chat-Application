import java.io.*;
import java.net.*;
import java.util.*;

public class receiver{
   static ArrayList<packet> list = new ArrayList<packet>();
  public static void main(String[] args) throws Exception {
    Socket s = new Socket("localhost", 8080);
    ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
    ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
    list = (ArrayList<packet>)ois.readObject();
    System.out.println(list.get(1).name);
    s.close();
  }
}
