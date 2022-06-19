package Room;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.HashMap;

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
        if (msg == "/quit") {
            leaveRoom(usrName);
        } else {
            for (Map.Entry<String, IUserChat> entry : userList.entrySet()) {
                try {
                    entry.getValue().deliverMsg(usrName, "USERMSG" + msg);
                } catch (Exception e) {
                    System.out.println("Room Error: " + e.getMessage());
                    e.printStackTrace();
                }
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
        for (Map.Entry<String, IUserChat> entry : userList.entrySet()) {
            try {
                entry.getValue().deliverMsg("Server", "ROOMCLOSE Room \"" + roomName + "\" was closed by the server.");
            } catch (Exception e) {
                System.out.println("Room Error: " + e.getMessage());
                e.printStackTrace();
            }
        }

        isOpen = false;
    }

    public String getRoomName() {
        return roomName;
    }

}
