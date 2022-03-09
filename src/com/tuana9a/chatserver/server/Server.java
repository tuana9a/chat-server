package com.tuana9a.chatserver.server;

import com.tuana9a.chatserver.utils.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static final int WIDTH = 500, HEIGHT = 350;
    public static final int COL = 10, ROW = 7;

    public static final int MAX_CONNECTIONS = 10;

    //CORE

    private int port;
    private Worker[] workers;
    private Listen listen;
    private int statusCount;
    private int currentWorking;

    //UI
    private JButton deployButton, stopListenButton, shutDownButton;
    private JTextField portField;
    private JLabel statusField;
    private JLabel[] workerStatusFields;
    private JCheckBox sameAddress;

    public Server() {
        port = 1406;
        currentWorking = 0;
        statusCount = 0;
        workers = new Worker[MAX_CONNECTIONS];
        initUIComponents();
        initUIListeners();
    }


    public void initUIComponents() {
        JFrame jFrame = new JFrame("GemDino Server");
        jFrame.setSize(WIDTH, HEIGHT);
        jFrame.setLocationRelativeTo(null);
        jFrame.setResizable(false);
        jFrame.setLayout(null);
        jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);


        JLabel statusLabel = new JLabel("STATUS: ");
        statusField = new JLabel("Offline");
        statusField.setForeground(Color.RED);

        JLabel workerBar = new JLabel("Workers");
        JLabel[] workerStatusLabels = new JLabel[MAX_CONNECTIONS];
        workerStatusFields = new JLabel[MAX_CONNECTIONS];
        for (int i = 0; i < MAX_CONNECTIONS; ++i) {
            workerStatusLabels[i] = new JLabel(String.valueOf(i));
            workerStatusLabels[i].setForeground(Color.BLUE);
            workerStatusFields[i] = new JLabel();
            workerStatusFields[i].setForeground(Color.RED);
        }

        deployButton = new JButton("DEPLOY");
        stopListenButton = new JButton("STOP LISTEN");
        shutDownButton = new JButton("SHUT DOWN");

        JLabel sameAddressLabel = new JLabel("ALLOW SAME ADDRESS");
        sameAddress = new JCheckBox();

        JLabel portLabel = new JLabel("PORT: ");
        portField = new JTextField(String.valueOf(port));


        JComponent[] comps = {
                statusLabel, statusField,
                stopListenButton, shutDownButton,
                workerBar,
                null,
                null,
                null, sameAddressLabel, sameAddress,
                portLabel, portField, deployButton,};

        int[] code = {
                5, 5,
                5, 5,
                10,
                10,
                10,
                6, 3, 1,
                2, 2, 6,};

        int[] code1 = new int[MAX_CONNECTIONS];
        for (int i = 0; i < MAX_CONNECTIONS; i++) {
            code1[i] = 1;
        }

        Utils.createUIs(
                0, 0,
                WIDTH, HEIGHT, COL, ROW,
                jFrame, comps, code);


        Utils.createUIs(
                0, HEIGHT / ROW * 3,
                WIDTH, HEIGHT / ROW, COL, 1,
                jFrame, workerStatusLabels, code1);

        Utils.createUIs(
                0, HEIGHT / ROW * 4,
                WIDTH, HEIGHT / ROW, COL, 1,
                jFrame, workerStatusFields, code1);

        jFrame.setVisible(true);
    }

    public void initUIListeners() {
        deployButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deploy();
            }
        });

        stopListenButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopListen();
            }
        });

        shutDownButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                shutDown();
            }
        });
    }

    public void updateStatus(String s) {
        statusCount++;
        statusField.setText("(" + statusCount + ")" + s);
    }


    public int currentWorking() {
        return currentWorking;
    }

    public int isServing(Socket s) {
        if (sameAddress.isSelected()) return -1;
        for (int i = 0; i < MAX_CONNECTIONS; i++) {
            Worker worker = workers[i];
            if (worker == null) continue;
            if (worker.getClientAddress().equals(s.getInetAddress())) {
                return i;
            }
        }
        return -1;
    }

    public void updateWorker(int id, Socket s) {
        workers[id].update(s);
        workerStatusFields[id].setText("work");
        ++currentWorking;
    }

    public void freeWorker(int id) {
        workers[id].stop();
        workerStatusFields[id].setText("free");
        --currentWorking;
    }

    public void newWorker(Socket s) {
        for (int i = 0; i < MAX_CONNECTIONS; ++i) {
            if (workers[i] == null) {
                workers[i] = new Worker(i, this, s);
                workers[i].start();
                workerStatusFields[i].setText("work");
                ++currentWorking;
                return;
            }
        }
    }


    public void onNewClient(Socket s) {
        for (int i = 0; i < MAX_CONNECTIONS; ++i) {
            if (workers[i] == null) {
                workers[i] = new Worker(i, this, s);
                workers[i].start();
                workerStatusFields[i].setText("work");
                ++currentWorking;
                return;
            } else if (workers[i].isFree()) {
                updateWorker(i,s);
                return;
            }
        }
    }

    public void onExistClient(int id, Socket s) {
        freeWorker(id);
        updateWorker(id,s);
    }

    public void onClientError(int id) {
        freeWorker(id);
    }



    public void parseAndForward(String clientMessage) {
//        System.out.println("clients: " + clientMessage);

        String[] pattens = clientMessage.split("\\|");
        int length = pattens.length;

        String fromId = pattens[0];
        String toId = pattens[length - 1];
        StringBuilder message = new StringBuilder();
        for (int i = 1; i < length - 1; i++) {
            message.append(pattens[i]);
        }

        Worker worker = workers[Integer.parseInt(toId)];
        if (worker != null) {
            worker.sendToClient(fromId + "|" + message);
        } else {
            workers[Integer.parseInt(fromId)].sendToClient("X|ID NOT EXIST");
        }
    }


    public void deploy() {
        shutDown();
        try {
            port = Integer.parseInt(portField.getText());

            ServerSocket serverSocket = new ServerSocket(port);

            listen = new Listen(this, serverSocket);
            listen.start();
            updateStatus("Online");
        } catch (Exception e) {
            updateStatus("Failed");
            e.printStackTrace();
        }
    }

    public void shutDown() {
        stopListen();
        resetAllWorkers();
        updateStatus("Down");
    }

    public void stopListen() {
        if (listen != null) {
            listen.stop();
        }
        updateStatus("Stop Listen");
    }

    public void resetAllWorkers() {
        for (int i = 0; i < MAX_CONNECTIONS; i++) {
            if (workers[i] != null) {
                workers[i].stop();
                workers[i] = null;
                workerStatusFields[i].setText("");
            }
        }
        currentWorking = 0;
    }
}
