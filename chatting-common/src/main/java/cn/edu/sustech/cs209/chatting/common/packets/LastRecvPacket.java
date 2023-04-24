package cn.edu.sustech.cs209.chatting.common.packets;

import cn.edu.sustech.cs209.chatting.common.Utils;
import cn.edu.sustech.cs209.chatting.common.packets.exceptions.DecodeException;
import cn.edu.sustech.cs209.chatting.common.packets.exceptions.EncodeException;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class LastRecvPacket extends BasePacket{

  private String chatId;

  private Long lastTimestamp;

  public LastRecvPacket() {
    super(PacketType.LAST_RECV);
  }

  public LastRecvPacket(String chatId, long lastTimestamp) {
    super(PacketType.LAST_RECV);
    this.chatId = chatId;
    this.lastTimestamp = lastTimestamp;
  }

  @Override
  protected ByteBuffer encode() throws EncodeException {
    ByteArrayOutputStream bs = new ByteArrayOutputStream();
    DataOutputStream ds = new DataOutputStream(bs);
    try {
      Utils.writeShortString(chatId, ds, StandardCharsets.UTF_8);
      ds.writeLong(lastTimestamp);
    } catch (IOException e) {
      throw new EncodeException();
    }
    return ByteBuffer.wrap(bs.toByteArray());
  }

  public String getChatId() {
    return chatId;
  }

  public Long getLastTimestamp() {
    return lastTimestamp;
  }

  @Override
  protected void decode(ByteBuffer buffer) throws DecodeException {
    chatId = Utils.readShortString(buffer, StandardCharsets.UTF_8);
    lastTimestamp = buffer.getLong();
  }
}
