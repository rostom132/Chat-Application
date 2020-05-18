package internetServer;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ClientMain {
    public static void main(String[] args) throws UnknownHostException {
        InetAddress ip = InetAddress.getByName("192.168.10.105");
        ClientHandler clientHandler = new ClientHandler(ip, 8818);
    }
}
