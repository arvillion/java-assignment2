package cn.edu.sustech.cs209.chatting.common.packets;

import cn.edu.sustech.cs209.chatting.common.packets.exceptions.DecodeException;
import cn.edu.sustech.cs209.chatting.common.packets.exceptions.EncodeException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class IndividualChatListPacket extends BasePacket {
  private List<String> individualList;
  public IndividualChatListPacket(List<String> list) {
    super(PacketType.INDIVIDUAL_CHAT_LIST);
    individualList = list;
  }
  public IndividualChatListPacket() {
    super(PacketType.INDIVIDUAL_CHAT_LIST);
    individualList = new ArrayList<>();
  }

  @Override
  public ByteBuffer encode() throws EncodeException {
    ByteArrayOutputStream bs = new ByteArrayOutputStream();
//    DataOutputStream ds = new DataOutputStream(bs);
    try {
      for (String username : individualList) {
        byte[] bytes = username.getBytes(StandardCharsets.UTF_8);
        if (bytes.length > Byte.MAX_VALUE) {
          throw new EncodeException();
        }
        bs.write(bytes.length);
        bs.write(bytes);
      }
    } catch(IOException e) {
      e.printStackTrace();
      throw new EncodeException();
    }
    return ByteBuffer.wrap(bs.toByteArray());
  }

  @Override
  protected void decode(ByteBuffer buffer) throws DecodeException {
    while (buffer.hasRemaining()) {
      int nameLen = buffer.get();
      byte[] nameBuf = new byte[nameLen];
      buffer.get(nameBuf);
      individualList.add(new String(nameBuf));
    }
  }

  public List<String> getIndividualList() {
    return individualList;
  }
}
