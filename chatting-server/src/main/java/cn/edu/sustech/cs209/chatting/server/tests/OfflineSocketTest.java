package cn.edu.sustech.cs209.chatting.server.tests;

import cn.edu.sustech.cs209.chatting.server.ClientHandler;
import cn.edu.sustech.cs209.chatting.server.Server;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class OfflineSocketTest {
  public static void main(String[] args) {
    Thread server = new Thread(new ServerThread());
    Thread client = new Thread(new ClientThread());
    server.start();
    client.start();
  }
}

class ClientThread implements Runnable {
  @Override
  public void run() {
    final int SERVER_PORT = 40023;
    try (Socket s = new Socket("localhost", SERVER_PORT)){
      Thread.sleep(500);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
class ServerThread implements Runnable {

  @Override
  public void run() {
    final int LISTEN_PORT = 40023;
    try {
      ServerSocket serverSocket = new ServerSocket(LISTEN_PORT);
      while (true) {
        Socket s = serverSocket.accept();
        OutputStream outputStream = s.getOutputStream();
        while (true) {
          outputStream.write(1);
          Thread.sleep(1000);
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
