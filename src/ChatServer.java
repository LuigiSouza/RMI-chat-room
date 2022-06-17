import java.rmi.registry.LocateRegistry;
import java.rmi.Naming;

import Server.IServerChat;
import Server.ServerChat;

public class ChatServer {
    public static void main(String[] args) {
        try {
            int port = 2020;
            IServerChat srv = new ServerChat();
            String objName = "rmi://localhost:" + port + "/Server";

            System.out.println("Registering server...");
            LocateRegistry.createRegistry(port).rebind("Server", srv);
            Naming.rebind(objName, srv);
            System.out.println("Server is running on port " + port + "...");
        } catch (Exception e) {
            System.out.println("Server Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
