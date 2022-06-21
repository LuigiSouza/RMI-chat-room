package Room;

import User.IUserChat;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IRoomChat extends Remote {
    public void sendMsg(String usrName, String msg) throws RemoteException;
    public void joinRoom(String usrName, IUserChat user) throws RemoteException;
    public void leaveRoom(String usrName) throws RemoteException;
    public void closeRoom() throws RemoteException;
    public String getRoomName() throws RemoteException;
    public void refreshUsers() throws RemoteException;
}