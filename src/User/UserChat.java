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
        // RFA4: No início, todo cliente, identificado pelo seu nome (usrName), deve contatar
        // o servidor e solicitar a lista de salas roomList.
        roomList = server.getRooms();
        room = null;
        roomName = "";

        this.createUI();

        // Listener para window close
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                try {
                    if (roomName != "")
                        leaveRoom();
                } catch (Exception err) {
                    System.out.println("Client error: " + err.getMessage());
                }
            }
        });
    }

    private void createUI() {
        frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("User");

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

        // RFA6: A lista de salas deve ser exibida na interface do usuário (GUI), 
        // para permitir a escolha da sala.
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

            if (selectedRoom.equals(roomName)) {
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
                } while (name.isEmpty());

                if (!roomName.isEmpty() && !roomName.equals(selectedRoom))
                    leaveRoom();
                
                userName = name;
                roomName = selectedRoom;
                
                // RFA7: Sempre que um usuário desejar entrar numa sala já existente ele deve 
                // solicitar a referência ao objeto remoto ao RMI Registry usando o nome da sala.
                room = (IRoomChat) Naming.lookup("rmi://" + server.getSocketValue() + "/Rooms/" + roomName);

                frame.setTitle(userName + " - " + roomName);
                textPane.setText("");
                textField.setEditable(true);
                textField.requestFocus();
                jListElement.setSelectedValue(null, false);

                // RFA7: após conhecer o objeto, deve invocar o método remoto 
                // joinRoom(String usrName) da respectiva sala.
                room.joinRoom(userName, this);
            } catch (Exception err) {
                refreshRooms();
                frame.setTitle("User");
                String message = err.getMessage();
                if (message.startsWith("REPEATEDNAME")) {
                    JOptionPane.showMessageDialog(frame, "This name is already in use", "Error joining room",
                            JOptionPane.ERROR_MESSAGE);
                    room = null;
                    roomName = null;
                } else
                    JOptionPane.showMessageDialog(frame, "Error joining room");
                
                selectedRoom = null;
                System.out.println("Client error: " + err.getMessage());
            }
        });


        // RFA8: Caso o usuário não encontre no servidor a sala desejada ele 
        // deve poder solicitar a criação de uma nova sala.
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
                System.out.println("Client error: " + err.getMessage());
            }
        });

        refreshRoomsButton.addActionListener(e -> {
            refreshRooms();
        });

        leaveRoomButton.addActionListener(e -> {
            if (room == null)
                return;

            try {
                leaveRoom();

                frame.setTitle("User");
            } catch (Exception err) {
                JOptionPane.showMessageDialog(frame, "Error leaving room", "Error leaving room",
                        JOptionPane.ERROR_MESSAGE);
                System.out.println("Client error: " + err.getMessage());
            }
        });

    }

    private void leaveRoom() throws RemoteException {
        room.leaveRoom(userName);
        userName = null;
        roomName = null;
        room = null;
        textField.setEditable(false);
    }

    private void sendMessage() {
        if (textField.getText().length() > 0) {
            try {
                // RFA9: Após pertencer a uma sala, o usuário deve enviar mensagens de texto à 
                // sala através da invocação ao método remoto sendMsg(String usrName, String msg) da sala.
                room.sendMsg(userName, textField.getText());
            } catch (RemoteException e) {
                System.out.println("Client error: " + e.getMessage());
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
            System.out.println("Client error: " + err.getMessage());
        }
    }

    // RFA10: Para receber mensagens, o processo do usuário deve implementar 
    // um método remoto deliverMsg(String senderName, String msg).
    public void deliverMsg(String senderName, String msg) throws RemoteException {
        if (msg.startsWith("ROOMINFO")) {
            appendToPane(null, msg.substring(9) + "\n", Color.BLUE);
        } else if (msg.startsWith("ROOMCLOSE")) {
            appendToPane(null, msg.substring(10) + "\n", Color.RED);
            leaveRoom();
            refreshRooms();
        } else if (msg.startsWith("USERMSG")) {
            appendToPane(senderName, msg.substring(8) + "\n", Color.BLACK);
        }
    }
}
