import java.rmi.registry.LocateRegistry;

import Server.ServerChat;

public class ChatServer {
    public static void main(String[] args) {
        try {
            ServerChat srv = new ServerChat();
            LocateRegistry.createRegistry(2020).rebind("Server", srv);
        } catch(Exception e) {
            System.out.println("Server Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
