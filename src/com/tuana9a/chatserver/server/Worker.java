package com.tuana9a.chatserver.server;

import com.tuana9a.chatserver.common.Receiver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class Worker implements Runnable {

    //EXPLAIN: một server có nhiều worker phục vụ nhiểu client
    // nhận từ client, gửi lên server
    // nhận từ server, gửi tới client

    private final int id;
    private Socket socket;
    private DataOutputStream dataOutputStream;

    private final Server myServer;

    private Receiver receiver;
    private volatile boolean running;
    private static final int REFRESH_TIME = 100;


    public Worker(int id, Server myServer, Socket socket) {
        this.id = id;
        this.myServer = myServer;
        this.socket = socket;
        try {
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            receiver = new Receiver(new DataInputStream(socket.getInputStream()));
            firstMessageToClient();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        if (running) return;
        running = true;
        new Thread(this).start();
    }

    public void stop() {
        if (!running) return;
        running = false;
        try {
            if (dataOutputStream != null) dataOutputStream.close();
            if (socket != null) socket.close();
            if (receiver != null) receiver.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        receiver.start();
        while (running) {
            try {
                if (receiver.hasNewMessage()) {
                    sendToServer(receiver.getNewestMessage());
                }
                Thread.sleep(REFRESH_TIME);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    //CAUTION chèn thêm id của mình để server biết
    public void sendToServer(String clientMessage) {
        myServer.parseAndForward(id + "|" + clientMessage);
    }

    public void sendToClient(String serverMessage) {
        try {
            dataOutputStream.writeUTF(serverMessage + "|" + id);
        } catch (IOException e) {
            myServer.onClientError(id);
            e.printStackTrace();
        }
    }


    public void firstMessageToClient() {
        sendToClient("X|HELLO WORLD!");
    }

    public InetAddress getClientAddress() {
        return socket.getInetAddress();
    }

    public void update(Socket socket) {
        stop();
        this.socket = socket;
        try {
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            receiver = new Receiver(new DataInputStream(socket.getInputStream()));
            firstMessageToClient();
        } catch (Exception e) {
            e.printStackTrace();
        }
        start();
    }

    public boolean isFree() {
        return !running;
    }
}
