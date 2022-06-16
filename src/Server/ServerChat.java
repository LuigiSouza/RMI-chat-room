package Server;

import java.util.ArrayList;

public class ServerChat implements IServerChat {
    private ArrayList<String> roomList;

    public ServerChat() {
        
    }

    @Override
    public ArrayList<String> getRooms() {
        return roomList;
    }

    @Override
    public void createRoom(String roomName) {

    }
}
