package cn.edu.sustech.cs209.chatting.server.packets.tests;

import cn.edu.sustech.cs209.chatting.server.packets.*;
import cn.edu.sustech.cs209.chatting.server.packets.exceptions.DecodeException;
import cn.edu.sustech.cs209.chatting.server.packets.exceptions.EncodeException;
import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class EncodeDecodeTest {
  @Test
  public void testIndividualChatListPacket() throws EncodeException, DecodeException {
    List<String> nameList = new ArrayList<>();
    nameList.add("Alice");
    nameList.add("Bob");
    nameList.add("Lucy");
    IndividualChatListPacket p = new IndividualChatListPacket(nameList);
    ByteBuffer buffer = p.toBytes();
    IndividualChatListPacket pp = new IndividualChatListPacket();
    pp.decodeFrom(buffer);

    Assert.assertArrayEquals(nameList.toArray(), pp.getIndividualList().toArray());
  }

  @Test
  public void testGroupChatListPacket() throws EncodeException, DecodeException {
    List<String> nameList = new ArrayList<>();
    nameList.add("g1");
    nameList.add("g2");
    nameList.add("g3aaa");
    GroupChatListPacket p = new GroupChatListPacket(nameList);
    ByteBuffer buffer = p.toBytes();
    GroupChatListPacket pp = new GroupChatListPacket();
    pp.decodeFrom(buffer);

    Assert.assertArrayEquals(nameList.toArray(), pp.getGroupList().toArray());
  }

  @Test
  public void testLoginPacket() throws EncodeException, DecodeException {
    String username = "arv";
    String password = "password123";
    LoginPacket p = new LoginPacket(username, password);
    ByteBuffer buffer = p.toBytes();
    LoginPacket pp = new LoginPacket();
    pp.decodeFrom(buffer);
    Assert.assertEquals(username, pp.getUsername());
    Assert.assertEquals(password, pp.getPassword());
  }

  @Test
  public void testRegisterPacket() throws EncodeException, DecodeException {
    String username = "arv";
    String password = "password123";
    RegisterPacket p = new RegisterPacket(username, password);
    ByteBuffer buffer = p.toBytes();
    RegisterPacket pp = new RegisterPacket();
    pp.decodeFrom(buffer);
    Assert.assertEquals(username, pp.getUsername());
    Assert.assertEquals(password, pp.getPassword());
  }

  @Test
  public void testOKPkt() throws EncodeException, DecodeException {
    OKPacket okPacket = new OKPacket();
    OKPacket okPacket1 = new OKPacket();
    okPacket1.decodeFrom(okPacket.toBytes());
  }

  @Test
  public void testFailPktWithoutReason() throws EncodeException, DecodeException {
    FailPacket failPacket = new FailPacket();
    FailPacket failPacket1 = new FailPacket();
    failPacket1.decodeFrom(failPacket.toBytes());
    Assert.assertEquals("", failPacket1.getReason());
  }

  @Test
  public void testFailPktWithReason() throws EncodeException, DecodeException {
    String prompt = "wrong password";
    FailPacket failPacket = new FailPacket(prompt);
    FailPacket failPacket1 = new FailPacket();
    failPacket1.decodeFrom(failPacket.toBytes());
    Assert.assertEquals(prompt, failPacket1.getReason());
  }
}
