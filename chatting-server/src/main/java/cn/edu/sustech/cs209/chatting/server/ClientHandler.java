package cn.edu.sustech.cs209.chatting.server;
import java.io.*;
import java.net.Socket;
import java.util.logging.Logger;


public class ClientHandler implements Runnable{
  private Socket s;
  private User user;
  private BufferedReader in;
  private PrintWriter out;
  private final Logger LOGGER = Logger.getLogger(ClientHandler.class.getName());

  public void onLogin(String username) {
    try {
      user = Server.userLogin(username);
    } catch (ServerException e) {
      LOGGER.warning(e.getMessage());
      sendFail("fail to login");
    }
    LOGGER.info(String.format("[%s] login successfully", username));
    sendOK();
  }

  public void sendOK() {

  }

  public void sendFail(String reason) {

  }

  public void sendFail() {

  }



  @Override
  public void run() {
//    MDC.put("client_port", String.valueOf(s.getPort()));
    try {
      configureLogger();
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }

//    try {
//      s.getInputStream().
//      in = new BufferedReader(new InputStreamReader(s.getInputStream()));
//      out = new PrintWriter(s.getOutputStream());
//      doService();
//    } catch (IOException e) {
//      throw new RuntimeException(e);
//    }
  }

  private void configureLogger() throws UnsupportedEncodingException {

  }

  private void doService() {
    try {
      while (true) {
        String command = in.readLine();
        LOGGER.info(command);
        if (command == null) {
          LOGGER.info("[{}] close connection");
          break;
        }
      }
    } catch (IOException e) {
      LOGGER.info("[{}] Client disconnected");
    } finally {
      try {
        s.close();
      } catch (IOException e) {
//        LOGGER.error();
      }
    }

  }

  public ClientHandler(Socket aSocket) {
    s = aSocket;
  }
}
