package User;

public interface IUserChat extends java.rmi.Remote {
    public void deliverMsg(String senderName, String msg);
}
