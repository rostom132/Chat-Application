package TestSystem;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;

public class iptest {
    public static void main(String[] args) throws UnknownHostException, SocketException {
//        InetAddress inetAddress = InetAddress.getLocalHost();
//        System.out.println("IP Address:- " + inetAddress);
//        System.out.println("Host Name:- " + inetAddress.getHostName());
//        try(final DatagramSocket socket = new DatagramSocket()){
//            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
//             String ip = socket.getLocalAddress().getHostAddress();
//            System.out.println(ip);
//        }
//        Inip = InetAddress.getByName("192.168.0.106");
//        Socket s = new Socket("192.168.0.106", 8818);
//        String ip = s.getLocalAddress().getHostAddress();
//        System.out.println(ip);
        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress("google.com", 80));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(socket.getLocalAddress().getHostAddress());
    }
}
