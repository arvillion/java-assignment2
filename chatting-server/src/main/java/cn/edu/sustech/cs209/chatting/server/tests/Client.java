package cn.edu.sustech.cs209.chatting.server.tests;

import cn.edu.sustech.cs209.chatting.common.messages.TextMessage;
import cn.edu.sustech.cs209.chatting.common.packets.*;
import cn.edu.sustech.cs209.chatting.common.packets.exceptions.DecodeException;
import cn.edu.sustech.cs209.chatting.common.packets.exceptions.EncodeException;
import cn.edu.sustech.cs209.chatting.common.packets.exceptions.InvalidPacketException;

import java.io.IOException;
import java.net.Socket;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class Client {
  private final String HOST;
  private final int PORT;
  private Socket socket;
  private PacketReader packetReader;
  private PacketWriter packetWriter;

  public Client(String host, int port) throws IOException {
    HOST = host;
    PORT = port;
    socket = new Socket(host, port);
    packetReader = new PacketReader(socket.getInputStream());
    packetWriter = new PacketWriter(socket.getOutputStream());
  }

  public void createGroup(String groupName, List<String> members) throws EncodeException, IOException {
    NewGroupPacket newGroupPacket = new NewGroupPacket(groupName, members);
    packetWriter.write(newGroupPacket);
  }

  public void login(String username, String password) throws EncodeException, IOException {
    LoginPacket loginPacket = new LoginPacket(username, password);
    packetWriter.write(loginPacket);
  }

  public void register(String username, String password) throws EncodeException, IOException {
    RegisterPacket registerPacket = new RegisterPacket(username, password);
    packetWriter.write(registerPacket);
  }

  public UUID sendTextMessage(String sentBy, String sendTo, String text) throws EncodeException, IOException {
    UUID uuid = UUID.randomUUID();
    Long timestamp = new Date().getTime();
    TextMessage textMessage = new TextMessage(uuid, timestamp, sentBy, sendTo, text);
    SendMessagePacket sendMessagePacket = new SendMessagePacket(textMessage);
    packetWriter.write(sendMessagePacket);
    return uuid;
  }

  public BasePacket nextPacket() throws IOException, DecodeException, InvalidPacketException {
    return packetReader.readPacket();
  }


}
