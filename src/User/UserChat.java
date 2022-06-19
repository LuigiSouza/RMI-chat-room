package User;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

import Room.IRoomChat;
import Server.IServerChat;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JScrollPane;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class UserChat extends UnicastRemoteObject implements IUserChat {
    private JFrame frame;
    private JTextField textField;
    private JTextPane textPane;
    private DefaultListModel<String> roomListElement;
    private String selectedRoom;

    private IServerChat server;
    private IRoomChat room;
    private String roomName;
    private ArrayList<String> roomList;
    private String userName;

    public UserChat(IServerChat server) throws RemoteException {
        super();

        this.server = server;
        roomList = server.getRooms();
        room = null;
        roomName = "";

        this.createUI();
    }

    private void createUI() {
        frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("Cliente");

        JPanel textFieldContainer = new JPanel();
        textFieldContainer.setLayout(new BorderLayout());
        JButton buttonSend = new JButton("Send");
        textField = new JTextField(50);
        textField.setEditable(false);
        textFieldContainer.add(textField, BorderLayout.WEST);
        textFieldContainer.add(buttonSend, BorderLayout.EAST);

        textPane = new JTextPane();
        textPane.setEditable(false);
        textPane.setContentType("text");
        textPane.setText("");
        textPane.setPreferredSize(new Dimension(200, 250));

        JLabel chatLabel = new JLabel("<html><b>Chat:</b></html>");
        chatLabel.setForeground(Color.DARK_GRAY);
        chatLabel.setHorizontalAlignment(JLabel.CENTER);
        JPanel chatContainer = new JPanel();
        chatContainer.setLayout(new BorderLayout());
        chatContainer.add(new JScrollPane(textPane), BorderLayout.CENTER);
        chatContainer.add(chatLabel, BorderLayout.NORTH);

        roomListElement = new DefaultListModel<String>();
        JList<String> jListElement = new JList<String>(roomListElement);
        jListElement.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jListElement.setLayoutOrientation(JList.VERTICAL);
        jListElement.setVisibleRowCount(-1);
        for (String room : roomList) {
            roomListElement.addElement(room);
        }

        JLabel roomsLabel = new JLabel("<html><b>Rooms:</b></html>");
        roomsLabel.setForeground(Color.DARK_GRAY);
        roomsLabel.setHorizontalAlignment(JLabel.CENTER);
        JScrollPane roomListScroller = new JScrollPane(jListElement);
        roomListScroller.setPreferredSize(new Dimension(125, 250));

        JButton joinRoomButton = new JButton("Join");
        JButton createRoomButton = new JButton("Create");
        JButton leaveRoomButton = new JButton("Leave");
        JButton refreshRoomsButton = new JButton("Refresh");
        JPanel buttonContainer = new JPanel();
        buttonContainer.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        buttonContainer.setLayout(new GridLayout(2, 2, 3, 3));
        buttonContainer.add(joinRoomButton);
        buttonContainer.add(createRoomButton);
        buttonContainer.add(leaveRoomButton);
        buttonContainer.add(refreshRoomsButton);

        JPanel roomListContainer = new JPanel();
        roomListContainer.setLayout(new BorderLayout());
        roomListContainer.add(roomsLabel, BorderLayout.NORTH);
        roomListContainer.add(roomListScroller, BorderLayout.CENTER);
        roomListContainer.add(buttonContainer, BorderLayout.SOUTH);

        frame.getContentPane().add(roomListContainer, BorderLayout.EAST);
        frame.getContentPane().add(textFieldContainer, BorderLayout.SOUTH);
        frame.getContentPane().add(chatContainer, BorderLayout.CENTER);

        frame.pack();
        frame.setVisible(true);

        // Room List Listener
        jListElement.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting())
                    selectedRoom = jListElement.getSelectedValue();
            }
        });

        // Button Listeners
        textField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                textField.setText("");
            }
        });

        joinRoomButton.addActionListener(e -> {
            if (selectedRoom == roomName) {
                JOptionPane.showMessageDialog(frame, "You are already in this room");
                return;
            }
            if (selectedRoom == null) {
                JOptionPane.showMessageDialog(frame, "Select a room to join");
                return;
            }
            try {
                String name;
                do {
                    name = JOptionPane.showInputDialog(frame, "Choose a screen name:", "Screen name selection",
                            JOptionPane.INFORMATION_MESSAGE);
                    if (name == null)
                        return;
                } while (name.isEmpty());
                roomName = selectedRoom;

                if (room != null) {
                    room.leaveRoom(userName);
                    textField.setEditable(false);
                }
                room = (IRoomChat) Naming.lookup("rmi://localhost:2020/Rooms/" + roomName);
                room.joinRoom(userName, this);

                textPane.setText("");
                textField.setEditable(true);
                textField.requestFocus();
                jListElement.setSelectedValue(null, false);
            } catch (Exception err) {
                String message = err.getCause().getMessage();
                if (message.startsWith("REPEATEDNAME"))
                    JOptionPane.showMessageDialog(frame, "This name is already in use", "Error joining room",
                            JOptionPane.ERROR_MESSAGE);
                else
                    JOptionPane.showMessageDialog(frame, "Error joining room");
                err.printStackTrace();
            }
        });

        createRoomButton.addActionListener(e -> {
            String roomName = JOptionPane.showInputDialog(frame, "Room name:", "Create Room",
                    JOptionPane.QUESTION_MESSAGE);
            if (roomName == null)
                return;
            roomName = roomName.strip();
            if (roomName.isEmpty())
                return;

            try {
                server.createRoom(roomName);
                roomList = server.getRooms();
                roomListElement.clear();
                for (String room : roomList) {
                    roomListElement.addElement(room);
                }
            } catch (Exception err) {
                String message = err.getCause().getMessage();
                if (message.startsWith("REPEATEDNAME"))
                    JOptionPane.showMessageDialog(frame, "This name is already in use", "Error creating room",
                            JOptionPane.ERROR_MESSAGE);
                else
                    JOptionPane.showMessageDialog(frame, "Error creating room");
                err.printStackTrace();
            }
        });

        refreshRoomsButton.addActionListener(e -> {
            try {
                roomList = server.getRooms();
                roomListElement.clear();
                for (String room : roomList) {
                    roomListElement.addElement(room);
                }
            } catch (Exception err) {
                JOptionPane.showMessageDialog(frame, "Error refreshing rooms");
                err.printStackTrace();
            }
        });

    }

    public void deliverMsg(String senderName, String msg) {

    }
}
