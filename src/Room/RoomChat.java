package Room;

import java.util.Map;

import User.IUserChat;

public class RoomChat {
    private String roomName;
    private Map<String, IUserChat> userList;

    public RoomChat(String roomName) {

    }
    
    public void sendMsg(String usrName, String msg) {

    }

    public void joinRoom(String usrName, IUserChat user) {

    }

    public void leaveRoom(String usrName) {

    }

    public void closeRoom() {

    }

    public String getRoomName() {
        return roomName;
    }

}
