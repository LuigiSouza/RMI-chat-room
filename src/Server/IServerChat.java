package Server;

public interface IServerChat extends java.rmi.Remote {
    public roomList getRooms();

    public void createRoom(String roomName);
}
