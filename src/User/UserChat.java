package User;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

import Room.IRoomChat;
import Server.IServerChat;

public class UserChat extends UnicastRemoteObject implements IUserChat {
    private IServerChat server;
    private IRoomChat room;
    private String roomName;
    private ArrayList<String> roomList;
    
    public UserChat(IServerChat server) throws RemoteException {
        super();
        this.server = server;
        roomList = server.getRooms(); 
        room = null;
        roomName = "";        
    }
    
    public void deliverMsg(String senderName, String msg) {
        
    }
}
