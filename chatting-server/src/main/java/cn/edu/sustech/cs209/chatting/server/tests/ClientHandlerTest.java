package cn.edu.sustech.cs209.chatting.server.tests;

import cn.edu.sustech.cs209.chatting.common.packets.*;
import cn.edu.sustech.cs209.chatting.common.packets.exceptions.DecodeException;
import cn.edu.sustech.cs209.chatting.common.packets.exceptions.EncodeException;
import cn.edu.sustech.cs209.chatting.server.Server;
import cn.edu.sustech.cs209.chatting.common.packets.exceptions.InvalidPacketException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ClientHandlerTest {
  final static int SERVER_PORT = 30045;
  @BeforeClass
  public static void setUpServer() throws IOException {

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
}
