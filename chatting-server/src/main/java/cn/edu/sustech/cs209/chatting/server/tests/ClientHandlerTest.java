package cn.edu.sustech.cs209.chatting.server.tests;

import cn.edu.sustech.cs209.chatting.common.messages.MessageType;
import cn.edu.sustech.cs209.chatting.common.messages.TextMessage;
import cn.edu.sustech.cs209.chatting.common.packets.*;
import cn.edu.sustech.cs209.chatting.common.packets.exceptions.DecodeException;
import cn.edu.sustech.cs209.chatting.common.packets.exceptions.EncodeException;
import cn.edu.sustech.cs209.chatting.server.Server;
import cn.edu.sustech.cs209.chatting.common.packets.exceptions.InvalidPacketException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.print.DocFlavor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.*;

public class ClientHandlerTest {
  int SERVER_PORT = 30045;
  final static String HOST = "localhost";
  @Before
  public void setUpServer() throws IOException {
    SERVER_PORT = new Random().nextInt(20000) + 10000;
    Server server = new Server(SERVER_PORT);
    Thread thread = new Thread(server);
    thread.start();
  }

  @Test
  public void testRegister() throws IOException, EncodeException, DecodeException, InvalidPacketException {
    Socket socket = new Socket("localhost", SERVER_PORT);
    String username = "johu";
    String password = "123@123";
    InputStream inputStream = socket.getInputStream();
    OutputStream outputStream = socket.getOutputStream();

    RegisterPacket registerPacket = new RegisterPacket(username, password);

    outputStream.write(registerPacket.toBytes().array());
    PacketReader packetReader = new PacketReader(inputStream);

    BasePacket basePacket = packetReader.readPacket();
    Assert.assertEquals(PacketType.OK, basePacket.getType());

    outputStream.write(registerPacket.toBytes().array());
    BasePacket basePacket1 = packetReader.readPacket();
    Assert.assertEquals(PacketType.FAIL, basePacket1.getType());
    FailPacket failPacket = (FailPacket) basePacket1;
    System.out.println(failPacket.getReason());

  }

  @Test
  public void testLogin() throws IOException, EncodeException, DecodeException, InvalidPacketException {
    Socket socket = new Socket("localhost", SERVER_PORT);
    InputStream inputStream = socket.getInputStream();
    OutputStream outputStream = socket.getOutputStream();

    PacketReader packetReader = new PacketReader(inputStream);
    PacketWriter packetWriter = new PacketWriter(outputStream);

    String username = "aoao";
    String password = "bobo";
    LoginPacket loginPacket = new LoginPacket(username, password);
    packetWriter.write(loginPacket);

    BasePacket basePacket = packetReader.readPacket();
    Assert.assertEquals(PacketType.FAIL, basePacket.getType());

    RegisterPacket registerPacket = new RegisterPacket(username, password);
    packetWriter.write(registerPacket);
    basePacket = packetReader.readPacket();
    Assert.assertEquals(PacketType.OK, basePacket.getType());

    packetWriter.write(loginPacket);
    basePacket = packetReader.readPacket();
    Assert.assertEquals(PacketType.OK, basePacket.getType());
  }

  @Test
  public void testGroupChatList() throws IOException, EncodeException, DecodeException, InvalidPacketException {
    Client client1 = new Client(HOST, SERVER_PORT);
    Client client2 = new Client(HOST, SERVER_PORT);
    Client client3 = new Client(HOST, SERVER_PORT);

    registerAndLogin(client1, "user1", "pwd");
    registerAndLogin(client2, "user2", "pwd");
    registerAndLogin(client3, "user3", "pwd");

    BasePacket basePacket;
    while((basePacket = client1.nextPacket()).getType() != PacketType.GROUP_CHAT_LIST);
    GroupChatListPacket groupChatListPacket = (GroupChatListPacket) basePacket;
    Assert.assertEquals(0, groupChatListPacket.getGroupList().size());

    List<String> members = new ArrayList<>();
    String groupName = "groupA";
    client1.createGroup(groupName, members);
    while((basePacket = client1.nextPacket()).getType() != PacketType.FAIL);
    Assert.assertEquals(PacketType.FAIL, basePacket.getType());

    members.add("user2");
    client1.createGroup(groupName, members);
    while((basePacket = client1.nextPacket()).getType() != PacketType.GROUP_CHAT_LIST);
    GroupChatListPacket groupChatListPacket1 = (GroupChatListPacket) basePacket;
    Assert.assertEquals(1, groupChatListPacket1.getGroupList().size());

  }

  @Test
  public void testIndividualChatList() throws IOException, EncodeException, DecodeException, InvalidPacketException {
    Client client1 = new Client(HOST, SERVER_PORT);
    Client client2 = new Client(HOST, SERVER_PORT);
    Client client3 = new Client(HOST, SERVER_PORT);

    registerAndLogin(client1, "user1", "pwd");
    BasePacket basePacket;
    while((basePacket = client1.nextPacket()).getType() != PacketType.INDIVIDUAL_CHAT_LIST);
    IndividualChatListPacket individualChatListPacket = (IndividualChatListPacket) basePacket;
    Assert.assertEquals(1, individualChatListPacket.getIndividualList().size());


    // user2 and user3 login
    registerAndLogin(client2, "user2", "pwd");
    registerAndLogin(client3, "user3", "pwd");

    while((basePacket = client1.nextPacket()).getType() != PacketType.INDIVIDUAL_CHAT_LIST);
    individualChatListPacket = (IndividualChatListPacket) basePacket;
    Assert.assertEquals(3, individualChatListPacket.getIndividualList().size());

  }

  @Test
  public void testTextMessage() throws IOException, EncodeException, DecodeException, InvalidPacketException {
    Client client1 = new Client(HOST, SERVER_PORT);
    Client client2 = new Client(HOST, SERVER_PORT);
    registerAndLogin(client1, "user1", "pwd");
    registerAndLogin(client2, "user2", "pwd");
    UUID uuid;
    String sentBy = "U:user1";
    String sendTo = "U:user2";
    String text = "hello";

    BasePacket basePacket;

    uuid = client1.sendTextMessage(sentBy, sendTo, text);
    while((basePacket = client1.nextPacket()).getType() != PacketType.ACK);
    AckPacket ackPacket = (AckPacket) basePacket;
    Assert.assertEquals(uuid, ackPacket.getMid());


    while((basePacket = client2.nextPacket()).getType() != PacketType.MSG_RECV);
    RecvMessagePacket recvMessagePacket = (RecvMessagePacket) basePacket;
    Assert.assertEquals(MessageType.TEXT, recvMessagePacket.getMessageType());
    TextMessage textMessage = (TextMessage) recvMessagePacket.getBaseMessage();
    Assert.assertEquals(uuid, textMessage.getUuid());
    Assert.assertEquals(sentBy, textMessage.getSentBy());
    Assert.assertEquals(sendTo, textMessage.getSendTo());
    Assert.assertEquals(text, textMessage.getText());


    client1.sendTextMessage(sentBy, "unknown", text);
    while((basePacket = client1.nextPacket()).getType() != PacketType.FAIL);
    Assert.assertTrue(true);

  }

  void register(Client client, String username, String password) throws IOException, EncodeException, DecodeException, InvalidPacketException {
    client.register(username, password);
    BasePacket basePacket = client.nextPacket();
    Assert.assertEquals(PacketType.OK, basePacket.getType());
  }

  void login(Client client, String username, String password) throws IOException, EncodeException, DecodeException, InvalidPacketException {
    client.login(username, password);
    BasePacket basePacket = client.nextPacket();
    Assert.assertEquals(PacketType.OK, basePacket.getType());
  }

  void registerAndLogin(Client client, String username, String password) throws EncodeException, IOException, DecodeException, InvalidPacketException {
    register(client, username, password);
    login(client, username,password);
  }



}
