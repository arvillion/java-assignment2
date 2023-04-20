package cn.edu.sustech.cs209.chatting.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

public class Main {



    public static void main(String[] args) throws IOException {
        final int LISTEN_PORT = 20086;

        ServerSocket server = new ServerSocket(LISTEN_PORT);
        System.out.println("Waiting for clients to connect...");
        while (true) {
            Socket s = server.accept();
            System.out.printf("[%d] Client connected\n", s.getPort());
            Thread t = new Thread(new ClientHandler(s));
            t.start();
        }

    }
}
