package cn.edu.sustech.cs209.chatting.common.packets.tests;

import cn.edu.sustech.cs209.chatting.common.messages.TextMessage;
import cn.edu.sustech.cs209.chatting.common.packets.*;
import cn.edu.sustech.cs209.chatting.common.packets.exceptions.DecodeException;
import cn.edu.sustech.cs209.chatting.common.packets.exceptions.EncodeException;
import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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

  @Test
  public void testSendMessagePkt() throws EncodeException, DecodeException {
    String sentBy = "U:user1";
    String sentTo = "U:user2";
    String text = "ok";
    TextMessage textMessage = new TextMessage(new Date().getTime(), sentBy, sentTo, text);
    SendMessagePacket sendMessagePacket = new SendMessagePacket(textMessage);
    SendMessagePacket sendMessagePacket1 = new SendMessagePacket();
    sendMessagePacket1.decodeFrom(sendMessagePacket.toBytes());
    TextMessage textMessage1 = (TextMessage) sendMessagePacket1.getBaseMessage();
    Assert.assertEquals(sentBy, textMessage1.getSentBy());
    Assert.assertEquals(sentTo, textMessage1.getSendTo());
    Assert.assertEquals(text, textMessage1.getText());
  }

  @Test
  public void testRecvMessagePkt() throws EncodeException, DecodeException {
    String sentBy = "U:user1";
    String sentTo = "U:user2";
    String text = "ok";
    TextMessage textMessage = new TextMessage(new Date().getTime(), sentBy, sentTo, text);
    RecvMessagePacket recvMessagePacket = new RecvMessagePacket(textMessage);
    RecvMessagePacket recvMessagePacket1 = new RecvMessagePacket();
    recvMessagePacket1.decodeFrom(recvMessagePacket.toBytes());
    TextMessage textMessage1 = (TextMessage) recvMessagePacket1.getBaseMessage();
    Assert.assertEquals(sentBy, textMessage1.getSentBy());
    Assert.assertEquals(sentTo, textMessage1.getSendTo());
    Assert.assertEquals(text, textMessage1.getText());
  }
}