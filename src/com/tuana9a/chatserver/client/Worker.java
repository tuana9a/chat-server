package com.tuana9a.chatserver.client;

import com.tuana9a.chatserver.common.Receiver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class Worker implements  Runnable{

    //EXPLAIN một client chỉ có một worker
    // nhận từ client, gửi lên server
    // nhận từ server, gửi tới client

    private static final int REFRESH_TIME = 100;


    private Client client;
    private Socket socket;
    private DataOutputStream dos;

    private volatile boolean running;
    private Receiver receiver;

    public Worker(Client client, Socket socket) {
        this.client = client;
        this.socket = socket;
        try {
            dos = new DataOutputStream(socket.getOutputStream());
            receiver = new Receiver(new DataInputStream(socket.getInputStream()));
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
                if(receiver.hasNewMessage()) {
                    sendToClient(receiver.getNewestMessage());
                }
                Thread.sleep(REFRESH_TIME);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }


    public void sendToServer(String clientMessage) {
        try {
            dos.writeUTF(clientMessage);
        } catch (IOException e) {
            client.onServerError("Server Error! Please Reconnect");
            e.printStackTrace();
        }
    }

    public void sendToClient(String serverMessage) {
        client.receiveAndParse(serverMessage);
    }


    public InetAddress getClientAddress() {
        return socket.getInetAddress();
    }

    public void update(Socket socket) {
        stop();
        this.socket = socket;
        try {
            dos = new DataOutputStream(socket.getOutputStream());
            receiver = new Receiver(new DataInputStream(socket.getInputStream()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        start();
    }
}
