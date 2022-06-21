package Server;

import java.util.ArrayList;
import java.util.List;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import Room.IRoomChat;
import Room.RoomChat;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ServerChat extends UnicastRemoteObject implements IServerChat {
    private ArrayList<String> roomList;
    private String ipAddress;
    private int port;

    // Swing Variables
    private JFrame frame;
    private JButton btnClose, btnOpen;
    private JLabel label;
    private JList<String> list;
    private DefaultListModel<String> listModel;
    private JScrollPane listScroller;

    public ServerChat(String ipAddress, int port) throws RemoteException {
        roomList = new ArrayList<String>();
        this.ipAddress = ipAddress;
        this.port = port;

        frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("Chat Server");
        createContentPane();
        frame.pack();
        frame.setVisible(true);
    }

    private void createContentPane() {

        listModel = new DefaultListModel<String>();

        list = new JList<String>();
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        list.setLayoutOrientation(JList.VERTICAL);
        list.setVisibleRowCount(-1);
        list.setModel(listModel);
        listScroller = new JScrollPane(list);
        listScroller.setPreferredSize(new Dimension(300, 200));

        frame.getContentPane().add(listScroller, BorderLayout.NORTH);

        // Buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(2, 1, 0, 3));
        btnOpen = new JButton("Open");
        btnOpen.setSize(300, 200);
        btnClose = new JButton("Close");
        buttonPanel.add(btnOpen);
        buttonPanel.add(btnClose);
        createBtnActionListeners();

        frame.getContentPane().add(buttonPanel, BorderLayout.CENTER);

        // Counter de salas
        label = new JLabel("Total Rooms: 0");
        label.setVerticalTextPosition(JLabel.BOTTOM);
        label.setHorizontalTextPosition(JLabel.CENTER);

        frame.getContentPane().add(label, BorderLayout.SOUTH);

    }

    private void createBtnActionListeners() {
        // Fechar as salas
        btnClose.addActionListener(e -> {
            List<String> deleteValues = list.getSelectedValuesList();
            for (String s : deleteValues) {
                try {
                    IRoomChat room = (IRoomChat) Naming.lookup("rmi://" + getSocketValue() + "/Rooms/" + s);
                    roomList.remove(s);
                    room.closeRoom();
                    Naming.unbind("rmi://" + getSocketValue() + "/Rooms/" + s);

                    listModel.removeElement(s);
                    label.setText("Total Rooms: " + listModel.size());
                    System.out.println("Room " + s + " closed");
                } catch (Exception err) {
                    System.out.println("Server error: " + err.getMessage());
                }
            }
        });

        // Criar nova sala
        btnOpen.addActionListener(e -> {
            String roomName = JOptionPane.showInputDialog(frame, "Room name:", "Create Room",
                    JOptionPane.QUESTION_MESSAGE);

            if (roomName == null)
                return;

            roomName = roomName.strip();

            if (roomName.isEmpty())
                return;

            try {
                createRoom(roomName);
            } catch (Exception err) {
                System.out.println("Server error: " + err.getMessage());
            }
        });
    }

    public ArrayList<String> getRooms() {
        System.out.println("Buscado a lista de salas");
        return roomList;
    }

    public void createRoom(String roomName) throws RemoteException {
        roomName = roomName.strip();

        if (roomList.contains(roomName))
            throw new RemoteException("REPEATEDNAME Room name already exists");

        try {
            RoomChat room = new RoomChat(roomName);
            roomList.add(roomName);
            listModel.addElement(roomName);

            Naming.rebind("rmi://" + getSocketValue() + "/Rooms/" + roomName, room);
            System.out.println("Room " + roomName + " created");
            label.setText("Total Rooms: " + listModel.size());
        } catch (Exception e) {
            System.out.println("Server Error: " + e.getMessage());
        }
    }

    public String getSocketValue() {
        return (ipAddress + ":" + port);
    }
}
