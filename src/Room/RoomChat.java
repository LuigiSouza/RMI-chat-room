package Room;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;

import User.IUserChat;

public class RoomChat extends UnicastRemoteObject implements IRoomChat {
    private String roomName;
    private Map<String, IUserChat> userList;
    boolean isOpen;

    public RoomChat(String roomName) throws RemoteException {
        super();
        this.roomName = roomName;
        this.userList = new HashMap<String, IUserChat>();
        this.isOpen = true;
    }

    public void sendMsg(String usrName, String msg) {
        for (Map.Entry<String, IUserChat> entry : userList.entrySet()) {
            try {
                entry.getValue().deliverMsg(usrName, "USERMSG " + msg);
            } catch (Exception e) {
                System.out.println("Room Error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void joinRoom(String usrName, IUserChat user) throws RemoteException {
        if (userList.containsKey(usrName))
            throw new RemoteException("REPEATEDNAME User " + usrName + " already in room " + roomName);

        userList.put(usrName, user);
        for (Map.Entry<String, IUserChat> entry : userList.entrySet()) {
            try {
                entry.getValue().deliverMsg(usrName, "ROOMINFO " + usrName + " has joined the room.");
            } catch (Exception e) {
                System.out.println("Room Error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void leaveRoom(String usrName) {
        if (!userList.containsKey(usrName))
            return;

        for (Map.Entry<String, IUserChat> entry : userList.entrySet()) {
            try {
                entry.getValue().deliverMsg(usrName, "ROOMINFO " + usrName + " left the room.");
            } catch (Exception e) {
                System.out.println("Room Error: " + e.getMessage());
                e.printStackTrace();
            }
        }

        userList.remove(usrName);
    }

    public void closeRoom() {
        Set<String> keys = new HashSet<String>(userList.keySet());
        for (String key : keys) {
            try {
                IUserChat usr = userList.get(key);
                usr.deliverMsg("Server", "ROOMCLOSE Room \"" + roomName + "\" was closed by the server.");
                userList.remove(key);
            } catch (Exception e) {
                System.out.println("Room Error: " + e.getMessage());
                e.printStackTrace();
            }
            System.out.println(keys);
        }

        isOpen = false;
    }

    public String getRoomName() {
        return roomName;
    }

}
