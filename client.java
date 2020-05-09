import java.net.*;
import java.io.*;
import java.util.*;
public class client{
  public static void main(String[] args) throws IOException{
      BufferedWriter os = null;
      BufferedReader is = null;
      Socket ss = new Socket("27.74.245.186", 8080);
      System.out.println("hello");
      clientInfo client = new clientInfo();
      client.getNickName();
      System.out.println(client.NickName+" Connected");
      try{
        os = new BufferedWriter(new OutputStreamWriter(ss.getOutputStream()));
        is = new BufferedReader(new InputStreamReader(ss.getInputStream()));
      } catch(IOException e){
        System.err.println("Couldn't get I/O for the connection to " + "27.74.245.186");
        return;
      }
      try{
        System.out.println("Sending message!");
        os.write("HELO! this is " + client.NickName);
        os.newLine(); // kết thúc dòng
        os.flush();  // đẩy dữ liệu đi.
        os.write("quit");
        os.newLine();
        os.flush();
        String responseLine;
           while ((responseLine = is.readLine()) != null) {
               System.out.println("Server: " + responseLine);
               if (responseLine.indexOf("OK") != -1) {
                   break;
               }
           }
        os.close();
        is.close();
        ss.close();
      }catch (IOException e) {
        System.err.println("IOException:  " + e);
       }
  }
}
