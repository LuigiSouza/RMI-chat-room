import java.rmi.Naming;

import javax.swing.JOptionPane;

import Server.IServerChat;
import User.UserChat;

public class ChatClient {
    public static void main(String[] args) {
        int port = 2020;
        String addr = "localhost";
        
        try {

            System.out.println("Connecting to server...");
            IServerChat srv = (IServerChat) Naming.lookup("rmi://"+ addr + ":" + port + "/Server");
            new UserChat(srv);
            System.out.println("Connected to server on port " + port + "...");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Could not reach address: " + addr + ":" + port, "Error",
                            JOptionPane.ERROR_MESSAGE);
        }
    }
}
