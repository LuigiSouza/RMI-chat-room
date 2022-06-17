import java.rmi.Naming;

import Server.IServerChat;
import User.UserChat;

public class ChatClient {
    public static void main(String[] args) {
        try {
            IServerChat srv = (IServerChat) Naming.lookup("//localhost:2020/Server");
            UserChat usr = new UserChat(srv);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
