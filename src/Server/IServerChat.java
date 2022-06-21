package Server;

import java.util.ArrayList;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IServerChat extends Remote {
    public ArrayList<String> getRooms() throws RemoteException;
    public void createRoom(String roomName) throws RemoteException;
    public String getSocketValue() throws RemoteException;
}
