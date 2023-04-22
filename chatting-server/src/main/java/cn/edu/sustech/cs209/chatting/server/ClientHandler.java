package cn.edu.sustech.cs209.chatting.server;
import cn.edu.sustech.cs209.chatting.common.messages.BaseMessage;
import cn.edu.sustech.cs209.chatting.common.messages.MessageType;
import cn.edu.sustech.cs209.chatting.common.messages.TextMessage;
import cn.edu.sustech.cs209.chatting.common.packets.PacketReader;
import cn.edu.sustech.cs209.chatting.common.packets.*;
import cn.edu.sustech.cs209.chatting.common.packets.exceptions.DecodeException;
import cn.edu.sustech.cs209.chatting.common.packets.exceptions.EncodeException;
import cn.edu.sustech.cs209.chatting.common.packets.exceptions.OfflineException;
import cn.edu.sustech.cs209.chatting.server.entities.User;
import cn.edu.sustech.cs209.chatting.server.exceptions.*;
import cn.edu.sustech.cs209.chatting.common.packets.exceptions.InvalidPacketException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.*;


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
      user = Server.userLogin(username, this);
      startNotification();
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

  private void handleCreateGroup(NewGroupPacket pkt) throws IOException {
    String groupName = pkt.getGroupName();
    List<String> members = pkt.getMembers();
    members.add(user.getName());
    try {
      Server.newGroup(groupName, members);
      sendOK();

    } catch (InvalidInputException e) {
      sendFail(e.getMessage());
    } catch (DuplicateGroupNameException e) {
      sendFail(String.format("Group name \"%s\" already exists", groupName));
    }
  }

  private void handleSendMessage(SendMessagePacket pkt) throws IOException {
    MessageType messageType = pkt.getBaseMessage().getMessageType();
    BaseMessage baseMessage = pkt.getBaseMessage();
    logger.info("Receive packet [SEND_MSG]: type={} sentBy={} sendTo={}", messageType, baseMessage.getSentBy(), baseMessage.getSendTo());

    try {
      switch (messageType) {
        case TEXT -> {
          Server.newTextMessage(user, (TextMessage) baseMessage);
          sendACK(baseMessage.getUuid());
        }
        default -> {
          throw new InvalidInputException("Unsupported message");
        }
      }

    } catch (InvalidInputException e) {
      sendFail(e.getMessage());
    } catch (ServerException e) {
      throw new RuntimeException(e);
    }
  }

  private void handleLastRecv(LastRecvPacket pkt) throws IOException {
    Long timestamp = pkt.getLastTimestamp();
    String target = pkt.getChatId();
    logger.info("Receive packet [LAST_RECV]: target={} timestamp={}", target, new Date(timestamp));
    try {
      List<BaseMessage> history = Server.fetchHistory(user, target, timestamp);

      logger.info("{} history messages found", history.size());

      // send messages in descending order of timestamp
      Collections.reverse(history);
      for (BaseMessage message : history) {
        sendMessage(message);
      }

    } catch (InvalidInputException e) {
      sendFail(e.getMessage());
    }

  }





  private void handleUnknown(BasePacket pkt) {
    logger.info("Receive unknown packet");
  }

  private void sendACK(UUID uuid) throws IOException {
    logger.info("Send packet [ACK]");
    AckPacket ackPacket = new AckPacket(uuid);
    packetSend(ackPacket);
  }

  private void sendIndividualChatList() throws IOException {
    List<String> onlineUsers = Server.getOnlineUsers();
    logger.info("Send packet [INDIVIDUAL_CHAT_LIST]: {}", onlineUsers);
    IndividualChatListPacket packet = new IndividualChatListPacket(onlineUsers);
    packetSend(packet);
  }

  private void sendGroupChatList() throws IOException {
    List<String> groups = Server.getOwnGroups(user);
    logger.info("Send packet [GROUP_CHAT_LIST]: {}", groups);
    GroupChatListPacket packet = new GroupChatListPacket(groups);
    packetSend(packet);
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

  private void quit() {
    logger.info("User \"{}\" quits", user.getName());
    Server.userQuit(user);
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

  private void startNotification() {
    final int GAP_UPDATE_CHAT_LIST = 1000;
    new Timer().schedule(new TimerTask() {
      @Override
      public void run() {
        try {
          sendIndividualChatList();
          sendGroupChatList();
        } catch (IOException e) {
          cancel();
//          throw new RuntimeException(e);
        }
      }
    }, 0, GAP_UPDATE_CHAT_LIST);
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
          case NEW_GROUP:
            NewGroupPacket newGroupPacket = (NewGroupPacket) pkt;
            handleCreateGroup(newGroupPacket);
            break;
          case MSG_SEND:
            SendMessagePacket sendMessagePacket = (SendMessagePacket) pkt;
            handleSendMessage(sendMessagePacket);
            break;
          case LAST_RECV:
            LastRecvPacket lastRecvPacket = (LastRecvPacket) pkt;
            handleLastRecv(lastRecvPacket);
            break;
          default:
            handleUnknown(pkt);
        }
      } catch (InvalidPacketException e) {
        logger.warn("Invalid packet");
      } catch (DecodeException de) {
        logger.warn("Fail to decode packet from bytes");
      } catch (IOException | OfflineException ioe) {
        quit();
        break;
      }
    }

  }

  public void sendMessage(BaseMessage message) {
    logger.info("Send packet [RECV_MSG]: type={} sentBy={} sendTo={} timestamp={}", message.getMessageType(), message.getSentBy(), message.getSendTo(), new Date(message.getTimestamp()));
    RecvMessagePacket packet = new RecvMessagePacket(message);
    try {
      packetSend(packet);
    } catch (IOException e) {
      quit();
    }
  }

  public ClientHandler(Socket aSocket) {
    s = aSocket;
    logger = LoggerFactory.getLogger("client" + s.getPort());
  }
}
