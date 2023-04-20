package cn.edu.sustech.cs209.chatting.server.packets;

import cn.edu.sustech.cs209.chatting.server.packets.exceptions.DecodeException;
import cn.edu.sustech.cs209.chatting.server.packets.exceptions.EncodeException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class GroupChatListPacket extends BasePacket {
  private List<String> groupList;
  public GroupChatListPacket(List<String> list) {
    type = PacketTypes.GROUP_CHAT_LIST;
    groupList = list;
  }
  public GroupChatListPacket() {
    type = PacketTypes.GROUP_CHAT_LIST;
    groupList = new ArrayList<>();
  }

  @Override
  public ByteBuffer encode() throws EncodeException {
    ByteArrayOutputStream bs = new ByteArrayOutputStream();
//    DataOutputStream ds = new DataOutputStream(bs);
    try {
      for (String username : groupList) {
        byte[] bytes = username.getBytes(StandardCharsets.UTF_8);
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
      groupList.add(new String(nameBuf));
    }
  }

  public List<String> getGroupList() {
    return groupList;
  }
}
