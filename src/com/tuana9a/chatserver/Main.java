package com.tuana9a.chatserver;

import com.tuana9a.chatserver.client.Client;
import com.tuana9a.chatserver.server.Server;

public class Main {
    public static void main(String[] args) {
        new Server();
        new Client();
        new Client();
    }
}
