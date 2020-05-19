package internetServer;

public class ServerMain {
    public static void main(String[] args) {
        String currentDirectory = System.getProperty("user.dir");
        System.out.println("Current path working: " + currentDirectory);
        int port = 8818;
        Server server = new Server(port);
        server.start();
    }
}
