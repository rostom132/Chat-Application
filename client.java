import java.io.*;
import java.net.*;

public class client{
  public static void main(String[] argv) throws Exception{
    int fileSize;
    Socket sr = new Socket("localhost", 8080);
    DataInputStream dis = new DataInputStream(sr.getInputStream());
    DataOutputStream dos = new DataOutputStream(sr.getOutputStream());
    fileSize = Integer.parseInt(dis.readUTF());
    if(fileSize > 0){
      dos.writeUTF("OK");
      byte[] b = new byte[fileSize];
      InputStream is = sr.getInputStream();
      File file = new File("./nhan/test.doc");
      FileOutputStream fos = new FileOutputStream(file);
      BufferedOutputStream bos = new BufferedOutputStream(fos);
      int a;
      while ((a = is.read()) != -1) {
        bos.write(a);
      }
      bos.flush();
      bos.close();
      fos.close();
      System.out.println("Done!");
    }
  }
}
