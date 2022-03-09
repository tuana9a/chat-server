package com.tuana9a.chatserver.common;

import java.io.DataInputStream;
import java.util.ArrayList;

public class Receiver implements Runnable {

    //EXPLAIN!
    // nhận tin nhắn lưu vào mảng
    // và trả tin nhắn khi được gọi
    private DataInputStream dataInputStream;

    private static final int REFRESH_TIME = 100;
    private volatile boolean running;

    private ArrayList<String> messages;
    private int totalMessage;
    private int currentReceived;

    public Receiver(DataInputStream dataInputStream) {
        this.dataInputStream = dataInputStream;
        this.messages = new ArrayList<>();
        currentReceived = 0;
        totalMessage = 0;
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
            if (dataInputStream != null) dataInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        currentReceived = 0;
        totalMessage = 0;
        while (running) {
            try {
                if (dataInputStream.available() != 0) {
                    messages.add(dataInputStream.readUTF());
                    ++totalMessage;
                }
                Thread.sleep(REFRESH_TIME);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public ArrayList<String> getAllMessages() {
        return messages;
    }

    public String getNewestMessage() {
        return messages.get(currentReceived++);
    }

    public boolean hasNewMessage() {
        return currentReceived != totalMessage;
    }
}
