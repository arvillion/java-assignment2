package cn.edu.sustech.cs209.chatting.server;
import cn.edu.sustech.cs209.chatting.common.packets.PacketReader;
import cn.edu.sustech.cs209.chatting.common.packets.*;
import cn.edu.sustech.cs209.chatting.common.packets.exceptions.DecodeException;
import cn.edu.sustech.cs209.chatting.common.packets.exceptions.EncodeException;
import cn.edu.sustech.cs209.chatting.server.exceptions.InvalidInputException;
import cn.edu.sustech.cs209.chatting.common.packets.exceptions.InvalidPacketException;
import cn.edu.sustech.cs209.chatting.server.exceptions.NotLoginException;
import cn.edu.sustech.cs209.chatting.server.exceptions.WrongUnameException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;


public class ClientHandler implements Runnable{
  private Socket s;
  private User user;
  private InputStream in;
  private OutputStream out;

  private Logger logger;

  private void handleLogin(LoginPacket pkt) throws IOException {
    String username = pkt.getUsername();
    String password = pkt.getPassword();
    logger.info("Receive packet [LOGIN]: uname={} pwd={}", username, password);
    // TODO: verify password
    try {
      user = Server.userLogin(username);
      sendOK();
    } catch (InvalidInputException e) {
      sendFail("Invalid input");
    } catch (WrongUnameException e) {
      sendFail("Wrong username or password");
    }
  }

  private void handleRegister(RegisterPacket pkt) throws IOException {
    String username = pkt.getUsername();
    String password = pkt.getPassword();
    logger.info("Receive packet [REGISTER]: uname={} pwd={}", username, password);
    // TODO: password support
    try {
      Server.userRegister(username);
      sendOK();
    } catch (InvalidInputException e) {
      sendFail("Invalid input");
    } catch (WrongUnameException e) {
      sendFail("Username already exists");
    }
  }

  private void handleUnknown() {

  }

  private void sendFail(String reason) throws IOException {
    logger.info("Send packet [FAIL]: {}", reason);
    FailPacket failPacket = new FailPacket(reason);
    packetSend(failPacket);
  }

  private void sendOK() throws IOException {
    logger.info("Send packet [OK]");
    OKPacket okPacket = new OKPacket();
    packetSend(okPacket);
  }

  private void packetSend(BasePacket basePacket) throws IOException {
    try {
      ByteBuffer buf = basePacket.toBytes();
      out.write(buf.array());
    } catch (EncodeException e) {
      throw new RuntimeException(e);
    }
  }

  private void requireLogined() throws NotLoginException {
    if (user == null) {
      logger.warn("Unauthorized");
      throw new NotLoginException();
    }
  }

  private void quit() throws NotLoginException {
    requireLogined();
    logger.info("User \"{}\" quits", user.getName());
    Server.userQuit(user.getName());
  }

  @Override
  public void run() {
    try {
      in = s.getInputStream();
      out = s.getOutputStream();
      doService();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void doService() throws IOException {
    PacketReader packetReader = new PacketReader(in);
    while (true) {


      try {
        BasePacket pkt = packetReader.readPacket();

        switch (pkt.getType()) {
          case LOGIN:
            LoginPacket loginPacket = (LoginPacket) pkt;
            handleLogin(loginPacket);
            break;
          case REGISTER:
            RegisterPacket registerPacket = (RegisterPacket) pkt;
            handleRegister(registerPacket);
            break;
          default:
            handleUnknown();
        }
      } catch (InvalidPacketException e) {
        logger.warn("Invalid packet");
      } catch (DecodeException de) {
        logger.warn("Fail to decode packet from bytes");
      } catch (IOException ioe) {
        try {
          quit();
          break;
        } catch (NotLoginException e) {
        }
      }
    }

  }

  public ClientHandler(Socket aSocket) {
    s = aSocket;
    logger = LoggerFactory.getLogger("client" + s.getPort());
  }
}
