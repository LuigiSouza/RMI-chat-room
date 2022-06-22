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
            String objName = "rmi://" + addr + ":" + port + "/Server";

            System.out.println("Registering server...");
            
            // RFA16: O servidor deve ser registrado no registro de RMI (rmiregistry) 
            // com o nome “Servidor” e usar a porta “2020” para escutar clientes. 
            // O registro deve executar na máquina do servidor.
            LocateRegistry.createRegistry(port).rebind("Server", srv);
            Naming.rebind(objName, srv);
            System.out.println("Server is running on address " + addr + ":" + port + "...");
        } catch (Exception e) {
            System.out.println("Server Error: " + e.getMessage());
        }
    }
}
