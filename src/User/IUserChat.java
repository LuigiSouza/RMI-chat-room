package User;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IUserChat extends Remote {
    public void deliverMsg(String senderName, String msg) throws RemoteException;
}
