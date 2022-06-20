package User;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IUserChat extends Remote {
    public void deliverMsg(String senderName, String msg) throws RemoteException;
    public void refreshRooms() throws RemoteException;
    public void leaveRoom() throws RemoteException;
}
