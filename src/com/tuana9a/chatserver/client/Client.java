package com.tuana9a.chatserver.client;

import com.tuana9a.chatserver.utils.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.net.Socket;

public class Client {
    public static final int WIDTH = 600, HEIGHT = 400;

    //CORE
    private int port;
    private int statusCount;
    private String hostAddress;
    private Worker worker;

    //UIS
    private JButton sendButton, connectButton, disconnectButton;
    private JLabel idSenderField, idField;
    private JLabel receiveField;
    private JLabel statusField;
    private JTextField hostAddressField, portField;
    private JTextField sendField;
    private JTextField idReceiverField;

    public Client() {
        port = 1406;
        hostAddress = "127.0.0.1";
        initUIComponents();
        initUIListeners();
    }

    public void initUIComponents() {
        JFrame jFrame = new JFrame("GemDino Client");
        jFrame.setSize(WIDTH,HEIGHT);
        jFrame.setLocationRelativeTo(null);
        jFrame.setResizable(false);
        jFrame.setLayout(null);
        jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        JLabel statusLabel = new JLabel("STATUS: ");
        statusField = new JLabel("Offline");
        statusField.setForeground(Color.RED);

        JLabel hostAddressLabel = new JLabel("SERVER'S ADDRESS");
        JLabel hostPortLabel = new JLabel("PORT");
        hostAddressField = new JTextField(hostAddress);
        portField = new JTextField(String.valueOf(port));
        connectButton = new JButton("CONNECT");
        disconnectButton = new JButton("DISCONNECT");

        JLabel idLabel = new JLabel("YOUR ID: ");
        idField = new JLabel("null");
        idField.setForeground(Color.RED);

        JLabel idSenderLabel = new JLabel("OTHER ID: ");
        idSenderField = new JLabel("null");
        idSenderField.setForeground(Color.RED);
        JLabel senderSayLabel = new JLabel("SAYS: ");
        receiveField = new JLabel("null");
        receiveField.setForeground(Color.RED);

        JLabel sendFieldLabel = new JLabel("YOUR MESSAGE");
        sendField = new JTextField("Type here");
        JLabel idReceiverLabel = new JLabel("TO ID");
        idReceiverField = new JTextField(String.valueOf(0));
        idReceiverField.setForeground(Color.RED);
        sendButton = new JButton("SEND");

        JComponent[] comps = {
                hostAddressLabel, hostPortLabel, null,
                hostAddressField, portField, connectButton,
                statusLabel, statusField, disconnectButton,

                idLabel, idField, null,
                idSenderLabel, idSenderField, senderSayLabel, receiveField,

                null,
                sendFieldLabel, idReceiverLabel, idReceiverField,
                sendField, sendButton,};

        int[] code = {
                7, 1, 2,
                7, 1, 2,
                2, 6, 2,

                1, 1, 8,
                1, 1, 1, 7,

                10,
                8, 1, 1,
                8, 2,};

        Utils.createUIs(
                0, 0,
                WIDTH, HEIGHT,
                10, 8,
                jFrame, comps, code);

        jFrame.setVisible(true);
    }

    public void initUIListeners() {
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                connect();
            }
        });

        disconnectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                disconnect();
            }
        });

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        sendField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == '\n') {
                    sendMessage();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });
    }

    public void updateStatus(String s) {
        statusCount++;
        statusField.setText( "(" + statusCount + ")" + s);
    }


    public void receiveAndParse(String serverMessage) {
//        System.out.println("server: " + serverMessage);
        String[] pattens = serverMessage.split("\\|");
        int length = pattens.length;

        String fromId = pattens[0];
        String toId = pattens[length - 1];
        StringBuilder message = new StringBuilder();
        for (int i = 1; i < length - 1; i++) {
            message.append(pattens[i]);
        }
        if (fromId.equals("X")) {
            idSenderField.setText("Server");
        } else {
            idSenderField.setText(fromId);
        }
        idField.setText(toId);
        receiveField.setText(String.valueOf(message));
    }

    public void sendMessage() {
        String message = sendField.getText();
        String toId = idReceiverField.getText().trim();
        if (!toId.matches("[0-9Xx]")) {
            updateStatus("Wrong ID");
            return;
        }
        try {
            worker.sendToServer(message + "|" + toId);
        }catch (Exception e) {
            //null pointer
            onServerError("Server Error! Reconnect");
        }
    }


    public void connect() {
        disconnect();
        try {
            hostAddress = hostAddressField.getText();
            port = Integer.parseInt(portField.getText());

            Socket socket = new Socket(hostAddress, port);
            worker = new Worker(this, socket);
            worker.start();
            updateStatus("Connected");
        } catch (IOException ex) {
            updateStatus("Server Down =))");
            ex.printStackTrace();
        }
    }

    public void disconnect() {
        if (worker != null) {
            worker.stop();
        }
        updateStatus("Offline");
    }

    public void onServerError(String error) {
        updateStatus(error);
    }
}
