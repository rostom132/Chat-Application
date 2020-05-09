import java.net.*;
import java.io.*;

public class server{
  public static void main(String[] args) throws IOException{
    clientInfo client[] = new clientInfo[10];
    int clientNum = 0;
    System.out.println("Open connection on port 8080");
    ServerSocket ss = new ServerSocket(8080);
    while(true){
      Socket s = ss.accept();
      new ServiceThread(s, clientNum++).start();
      System.out.println("client connected");
    }
  }
  private static class ServiceThread extends Thread {
    private int clientNum;
    private Socket s;
    public ServiceThread(Socket s, int clientNum) {
      this.clientNum = clientNum;
      this.s = s;
    }
    @Override
    public void run(){
      try{
        BufferedReader is = new BufferedReader(new InputStreamReader(s.getInputStream()));
        BufferedWriter os = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
        while(true){
          String line = is.readLine();
          os.write(">>"+line);
          os.newLine();
          os.flush();
          if (line.equals("QUIT")) {
            os.write(">> OK");
            os.newLine();
            os.flush();
            break;
          }
        }
      } catch(IOException e){
        System.out.println(e);
        e.printStackTrace();
      }
    }
  }
}
