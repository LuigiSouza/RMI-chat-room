package Server;

import java.util.ArrayList;
import java.util.List;

import Room.IRoomChat;
import Room.RoomChat;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


public class ServerChat extends UnicastRemoteObject implements IServerChat {
    private ArrayList<String> roomList;

    // Swing Variables
    private JFrame frame;
    private JPanel panel;
    private JButton btnClose, btnOpen;
    private JLabel label;
    private JList<String> list;
    private DefaultListModel<String> listModel;
    private JScrollPane listScroller;

    public ServerChat() throws RemoteException {
        roomList = new ArrayList<String>();

        frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("Chat Server");
        createContentPane();
        frame.pack();
        frame.setVisible(true);
    }

    private void createContentPane() {
        Container pane = frame.getContentPane();
        pane.setLayout(new GridBagLayout());

        listModel = new DefaultListModel<String>();

        list = new JList<String>();
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        list.setLayoutOrientation(JList.VERTICAL);
        list.setVisibleRowCount(-1);

        GridBagConstraints constr = new GridBagConstraints();
        constr.fill = GridBagConstraints.HORIZONTAL;
        constr.gridwidth = 3;
        constr.gridx = 0;
        constr.gridy = 0;

        pane.add(list, constr);

        listScroller = new JScrollPane(list);
        listScroller.setPreferredSize(new Dimension(300, 200));

        pane.add(listScroller);

        // Buttons
        btnOpen  = new JButton("Open");
        btnClose = new JButton("Close");
        createBtnActionListeners();

        constr.fill = GridBagConstraints.HORIZONTAL;
        constr.gridx = 0;
        constr.gridy = 1;
        pane.add(btnOpen, constr);
        constr.gridy = 2;
        pane.add(btnClose, constr);
        
        // Counter de salas
        label = new JLabel("Total Rooms: 0");
        label.setVerticalTextPosition(JLabel.BOTTOM);
        label.setHorizontalTextPosition(JLabel.CENTER);
        
        constr.gridx = 0;
        constr.gridy = 3;
        pane.add(label, constr);
    }

    private void createBtnActionListeners() {
        btnClose.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                List<String> deleteValues = list.getSelectedValuesList();
                for(String s : deleteValues) {
                    try {
                        IRoomChat room = (IRoomChat) Naming.lookup("//localhost:2020/Salas/" + s);
                        room.closeRoom();
                        Naming.unbind("//localhost:2020/Salas/" + s);
                        
                        roomList.remove(s);
                        listModel.removeElement(s);
                    } catch (Exception ex) {
                        System.out.println("Server error: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                }
            }
        });

        btnOpen.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String newRoomName = "Teste";
                // createRoom();
            }
        });
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
