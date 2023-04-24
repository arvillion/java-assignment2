package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.client.exceptions.SyncException;
import cn.edu.sustech.cs209.chatting.common.messages.TextMessage;
import cn.edu.sustech.cs209.chatting.common.packets.*;
import cn.edu.sustech.cs209.chatting.common.packets.exceptions.DecodeException;
import cn.edu.sustech.cs209.chatting.common.packets.exceptions.EncodeException;
import cn.edu.sustech.cs209.chatting.common.packets.exceptions.InvalidPacketException;
import cn.edu.sustech.cs209.chatting.common.packets.exceptions.OfflineException;
import javafx.scene.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class Client implements Runnable{
  private Socket socket;
  private PacketReader packetReader;
  private PacketWriter packetWriter;
  private Logger logger = LoggerFactory.getLogger(getClass());
  private boolean isLogined = false;
  private String username;

  private Controller controller;
  private Node msgBlockedNode;
  private Node blockedNode;


  public Client(Controller controller, String host, int port) throws IOException {
    socket = new Socket(host, port);
    packetReader = new PacketReader(socket.getInputStream());
    packetWriter = new PacketWriter(socket.getOutputStream());
    this.controller = controller;
  }

  public BasePacket nextPacket() throws OfflineException {
    BasePacket basePacket;
    try {
      basePacket = packetReader.readPacket();
    } catch (IOException e) {
      logger.warn("[IO Exception] convert to Offline Exception");
      throw new OfflineException();
    } catch (InvalidPacketException e) {
      logger.warn("[Invalid Packet Exception] return null");
      return null;
    } catch (DecodeException e) {
      logger.warn("[Decode Exception] return null");
      return null;
    }
    return basePacket;
  }

  public boolean sendPacket(BasePacket packet) throws OfflineException {
    try {
      packetWriter.write(packet);
    } catch (EncodeException e) {
      return false;
    } catch (IOException e) {
      throw new OfflineException();
    }
    return true;
  }

  public void createGroup(Node node, String groupName, List<String> members) throws OfflineException, SyncException {
    NewGroupPacket newGroupPacket = new NewGroupPacket(groupName, members);
    if (!sendPacket(newGroupPacket)) {
      throw new SyncException("Fail to create a group");
    }
    blockedNode = node;
  }

  public void login(String username, String password) throws OfflineException, SyncException {
    LoginPacket loginPacket = new LoginPacket(username, password);
    if (!sendPacket(loginPacket)) {
      throw new SyncException("Fail to login");
    }
    BasePacket basePacket = nextPacket();
    isLogined = false;
    if (basePacket == null) {
      throw new SyncException("Fail to login");
    } else {
      if (basePacket.getType() == PacketType.OK) {
        isLogined = true;
        this.username = username;
        return;
      } else if (basePacket.getType() == PacketType.FAIL) {
        throw new SyncException(((FailPacket) basePacket).getReason());
      } else {
        throw new SyncException("Fail to login");
      }
    }
  }

  public void register(String username, String password) throws OfflineException, SyncException {
    RegisterPacket registerPacket = new RegisterPacket(username, password);
    if (!sendPacket(registerPacket)) {
      throw new SyncException("Fail to register");
    }
    BasePacket basePacket = nextPacket();
    if (basePacket == null) {
      throw new SyncException("Fail to register");
    }
    switch (basePacket.getType()) {
      case OK: return;
      case FAIL: throw new SyncException(((FailPacket) basePacket).getReason());
      default: throw new SyncException("Fail to register");
    }
  }

  public TextMessage sendTextMessage(Node node, String sentBy, String sendTo, String text) throws OfflineException, SyncException{
    UUID uuid = UUID.randomUUID();
    Long timestamp = new Date().getTime();
    TextMessage textMessage = new TextMessage(uuid, timestamp, sentBy, sendTo, text);
    SendMessagePacket sendMessagePacket = new SendMessagePacket(textMessage);
    if (!sendPacket(sendMessagePacket)) {
      throw new SyncException("Fail to send");
    }
    msgBlockedNode = node;
    return textMessage;
  }



  public void history(String target, Long timestamp) throws EncodeException, IOException {
    LastRecvPacket lastRecvPacket = new LastRecvPacket(target, timestamp);
    packetWriter.write(lastRecvPacket);
  }

  public void close() throws IOException {
    socket.close();
  }

  public boolean isLogined() {
    return isLogined;
  }

  public void setLogined(boolean logined) {
    isLogined = logined;
  }

  @Override
  public void run() {
    while (true) {
      try {
        BasePacket basePacket = nextPacket();
        switch (basePacket.getType()) {
          case INDIVIDUAL_CHAT_LIST:
            IndividualChatListPacket individualChatListPacket = (IndividualChatListPacket) basePacket;
            List<String> individuals = individualChatListPacket.getIndividualList().stream().filter(s -> !username.equals(s)).collect(Collectors.toList());
            controller.setIndividuals(individuals);
            break;
          case GROUP_CHAT_LIST:
            GroupChatListPacket groupChatListPacket = (GroupChatListPacket) basePacket;
            List<String> groups = groupChatListPacket.getGroupList();
            controller.setGroups(groups);
            break;
          case ACK:
            msgBlockedNode.fireEvent(new AckEvent());
            break;
          case MSG_RECV:
            RecvMessagePacket recvMessagePacket = (RecvMessagePacket) basePacket;
            controller.receiveMessage(recvMessagePacket.getBaseMessage());
            break;
          case OK:
            blockedNode.fireEvent(new OKEvent());
            break;

        }
      } catch (OfflineException e) {
        controller.onServerOffline();
        break;
      }
    }
  }
}
