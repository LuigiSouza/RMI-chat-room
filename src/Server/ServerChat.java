package Server;

import java.util.ArrayList;

import Room.RoomChat;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ServerChat extends UnicastRemoteObject implements IServerChat {
    private ArrayList<String> roomList;

    public ServerChat() throws RemoteException {
        roomList = new ArrayList<String>();
    }

    @Override
    public ArrayList<String> getRooms() {
        System.out.println("Buscado a lista de salas");
        return roomList;
    }

    @Override
    public void createRoom(String roomName) {
        try {
            RoomChat room = new RoomChat(roomName);

            roomList.add(roomName);
            Naming.rebind("//localhost:2020/Rooms/", room);
            System.out.println("Criado o room " + roomName);
        } catch (Exception e) {
            System.out.println("Server Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
