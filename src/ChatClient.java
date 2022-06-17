import java.rmi.Naming;

import Server.IServerChat;
import User.UserChat;

public class ChatClient {
    public static void main(String[] args) {
        try {
            int port = 2020;

            System.out.println("Connecting to server...");
            IServerChat srv = (IServerChat) Naming.lookup("rmi://localhost:" + port + "/Server");
            new UserChat(srv);
            System.out.println("Connected to server on port " + port + "...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
