package cn.edu.sustech.cs209.chatting.common.packets;

import cn.edu.sustech.cs209.chatting.common.Utils;
import cn.edu.sustech.cs209.chatting.common.packets.exceptions.DecodeException;
import cn.edu.sustech.cs209.chatting.common.packets.exceptions.EncodeException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class NewGroupPacket extends BasePacket {

  private List<String> members;
  private String groupName;
  public NewGroupPacket() {
    super(PacketType.NEW_GROUP);
    members = new ArrayList<>();
  }

  public NewGroupPacket(String groupName, List<String> mems) {
    super(PacketType.NEW_GROUP);
    members = mems;
    this.groupName = groupName;
  }

  public String getGroupName() {
    return groupName;
  }

  public List<String> getMembers() {
    return members;
  }

  @Override
  protected ByteBuffer encode() throws EncodeException {
    ByteArrayOutputStream bs = new ByteArrayOutputStream();

    try {
      Utils.writeShortString(groupName, bs, StandardCharsets.UTF_8);
      for (String user : members) {
        Utils.writeShortString(user, bs, StandardCharsets.UTF_8);
      }
    } catch (IOException e) {
      throw new EncodeException();
    }
    return ByteBuffer.wrap(bs.toByteArray());
  }

  @Override
  protected void decode(ByteBuffer buffer) throws DecodeException {
    groupName = Utils.readShortString(buffer, StandardCharsets.UTF_8);
    while (buffer.hasRemaining()) {
      members.add(Utils.readShortString(buffer, StandardCharsets.UTF_8));
    }
  }
}
