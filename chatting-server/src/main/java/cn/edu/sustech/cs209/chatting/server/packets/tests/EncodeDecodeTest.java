package cn.edu.sustech.cs209.chatting.server.packets.tests;

import cn.edu.sustech.cs209.chatting.server.packets.DecodeException;
import cn.edu.sustech.cs209.chatting.server.packets.EncodeException;
import cn.edu.sustech.cs209.chatting.server.packets.IndividualChatListPacket;
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
}
