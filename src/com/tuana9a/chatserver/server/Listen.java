package com.tuana9a.chatserver.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Listen implements Runnable {

    //EXPLAIN
    // nghe rồi báo cho server

    private final Server server;
    private final ServerSocket serverSocket;

    private volatile boolean running;

    public Listen(Server server, ServerSocket serverSocket) {
        this.server = server;
        this.serverSocket = serverSocket;
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
            if (serverSocket != null) serverSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (running && server.currentWorking() < Server.MAX_CONNECTIONS) {
            try {
//                System.out.println(server.currentClientNumber());
                Socket s = serverSocket.accept();
                int id = server.isServing(s);
                if (id == -1) {
                    server.onNewClient(s);
                } else {
                    server.onExistClient(id, s);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        server.stopListen();
    }
}
