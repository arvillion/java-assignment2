package cn.edu.sustech.cs209.chatting.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Service {
  private final String SERVER_HOST = "localhost";
  private final int SERVER_PORT = 20086;
  private Scanner in;
  private PrintWriter out;
  private Socket socket;

  private final Logger LOGGER = Logger.getLogger(Service.class.getName());

  public Service () throws IOException {

    LOGGER.setUseParentHandlers(false);
    ConsoleHandler consoleHandler = new ConsoleHandler();
    consoleHandler.setEncoding("UTF-8");
    consoleHandler.setFormatter(new SimpleFormatter());
    LOGGER.addHandler(consoleHandler);

    try {
      socket = new Socket(SERVER_HOST, SERVER_PORT);

      LOGGER.info(String.format("Server(%s:%d) connected", SERVER_HOST, SERVER_PORT));
      InputStream inputStream = socket.getInputStream();
      OutputStream outputStream = socket.getOutputStream();
      in = new Scanner(inputStream);
      out = new PrintWriter(outputStream);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void hello() {
    LOGGER.info("Send hello to the server");
    out.println("HELLO");
    out.flush();
  }
}
