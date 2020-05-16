import java.io.*;
import java.net.*;

public class server{
  public static void main(String[] argv) throws Exception{
    ServerSocket s = new ServerSocket(8080);
    Socket sr = s.accept();
    DataInputStream dis = new DataInputStream(sr.getInputStream());
    DataOutputStream dos = new DataOutputStream(sr.getOutputStream());
    File file = new File("code.zip");
    if (!file.exists() || !file.isFile()) return;
    int fileSize;
    fileSize = (int)file.length();
    byte b[] = new byte[fileSize];
    FileInputStream fis = new FileInputStream(file);
    BufferedInputStream bis = new BufferedInputStream(fis);
    dos.writeUTF(Integer.toString(fileSize));
    if(dis.readUTF().equals("OK")){
      bis.read(b, 0, b.length);
      OutputStream os = sr.getOutputStream();
      os.write(b, 0, b.length);
      os.flush();
    }
  }
}
