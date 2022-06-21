import java.rmi.registry.LocateRegistry;
import java.rmi.Naming;

import Server.IServerChat;
import Server.ServerChat;

public class ChatServer {
    public static void main(String[] args) {
        int port = 2020;
        String addr = "localhost";
        
        try {
            IServerChat srv = new ServerChat(addr, port);
            String objName = "rmi://"+ addr + ":" + port + "/Server";

            System.out.println("Registering server...");
            LocateRegistry.createRegistry(port).rebind("Server", srv);
            Naming.rebind(objName, srv);
            System.out.println("Server is running on address " + addr + ":" + port + "...");
        } catch (Exception e) {
            System.out.println("Server Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
