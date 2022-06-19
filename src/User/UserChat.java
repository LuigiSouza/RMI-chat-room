package User;

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
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JScrollPane;
import javax.swing.DefaultListModel;
import javax.swing.ListSelectionModel;

public class UserChat extends UnicastRemoteObject implements IUserChat {
    private JFrame frame;
    private JTextField textField;
    private JTextPane textPane;
    private DefaultListModel<String> roomListElement;

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
        JButton btnSend = new JButton("Send");
        textField = new JTextField(50);
        textField.setEditable(true);
        textFieldContainer.add(textField, BorderLayout.WEST);
        textFieldContainer.add(btnSend, BorderLayout.EAST);

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
        JPanel buttonContainer = new JPanel();
        buttonContainer.setLayout(new FlowLayout());
        buttonContainer.add(joinRoomButton);
        buttonContainer.add(createRoomButton);
        buttonContainer.add(leaveRoomButton);

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

        // Send on enter then clear to prepare for next message
        textField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                textField.setText("");
            }
        });
    }

    public void deliverMsg(String senderName, String msg) {

    }
}
