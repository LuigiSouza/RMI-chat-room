package User;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

import Room.IRoomChat;
import Server.IServerChat;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;

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
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class UserChat extends UnicastRemoteObject implements IUserChat {
    private JFrame frame;
    private JTextField textField;
    private JTextPane textPane;
    private DefaultListModel<String> roomListElement;
    private String selectedRoom;

    private Style style;

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
        
        frame.addWindowListener( new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                try {
                    if (roomName != "")
                        leaveRoom();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
       });
    }

    private void createUI() {
        frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("Usuário");

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
        style = textPane.addStyle("Style", null);

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
        for (String r : roomList) {
            roomListElement.addElement(r);
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
        textField.addActionListener(e -> {
            sendMessage();
        });

        buttonSend.addActionListener(e -> {
            sendMessage();
        });

        joinRoomButton.addActionListener(e -> {
            if (selectedRoom == null) {
                JOptionPane.showMessageDialog(frame, "Select a room to join");
                return;
            }
            
            if (selectedRoom == roomName) {
                JOptionPane.showMessageDialog(frame, "You are already in this room");
                return;
            }
            try {
                String name;
                do {
                    name = JOptionPane.showInputDialog(frame, "Choose a screen name:", "Screen name selection",
                            JOptionPane.INFORMATION_MESSAGE);
                    if (name == null)
                        return;
                    userName = name;
                } while (name.isEmpty());

                if (room != null)
                    leaveRoom();

                roomName = selectedRoom;
                room = (IRoomChat) Naming.lookup("rmi://" + server.getSocketValue() + "/Rooms/" + roomName);

                frame.setTitle(userName + " - " + roomName);
                textPane.setText("");
                textField.setEditable(true);
                textField.requestFocus();
                jListElement.setSelectedValue(null, false);

                room.joinRoom(userName, this);
            } catch (Exception err) {
                String message = err.getCause().getMessage();
                if (message.startsWith("REPEATEDNAME")) {
                    JOptionPane.showMessageDialog(frame, "This name is already in use", "Error joining room",
                            JOptionPane.ERROR_MESSAGE);
                    room = null;
                    roomName = null;
                } else
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
                refreshRooms();
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

        refreshRoomsButton.addActionListener(e -> { refreshRooms(); });

        leaveRoomButton.addActionListener(e -> {
            if (room == null)
                return;

            try {
                leaveRoom();

                frame.setTitle("Usuário");
            } catch (Exception err) {
                JOptionPane.showMessageDialog(frame, "Error leaving room", "Error leaving room",
                        JOptionPane.ERROR_MESSAGE);
                err.printStackTrace();
            }
        });

    }

    public void leaveRoom() throws RemoteException {
        room.leaveRoom(userName);
        userName = null;
        roomName = null;
        selectedRoom = null;
        room = null;
        textField.setEditable(false);
    }

    private void sendMessage() {
        if (textField.getText().length() > 0) {
            try {
                room.sendMsg(userName, textField.getText());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            textField.setText("");
        }
    }

    private void appendToPane(String sender, String msg, Color c) {
        StyledDocument sd = textPane.getStyledDocument();
        StyleConstants.setForeground(style, c);
        String text = sender == null ? msg : "<" + sender + "> " + msg;
        try {
            sd.insertString(sd.getLength(), text, style);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void refreshRooms() {
        try {
            roomList = server.getRooms();
            roomListElement.clear();
            
            for (String r : roomList) {
                roomListElement.addElement(r);
            }
        } catch (Exception err) {
            JOptionPane.showMessageDialog(frame, "Error refreshing rooms");
            err.printStackTrace();
        }
    }

    public void deliverMsg(String senderName, String msg) {
        if (msg.startsWith("ROOMINFO")) {
            appendToPane(null, msg.substring(9) + "\n", Color.BLUE);
        } else if (msg.startsWith("ROOMCLOSE")) {
            appendToPane(null, msg.substring(10) + "\n", Color.RED);
        } else if (msg.startsWith("USERMSG")) {
            appendToPane(senderName, msg.substring(8) + "\n", Color.BLACK);
        }
    }
}
