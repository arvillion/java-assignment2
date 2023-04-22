package cn.edu.sustech.cs209.chatting.common.packets;

import cn.edu.sustech.cs209.chatting.common.Utils;
import cn.edu.sustech.cs209.chatting.common.packets.exceptions.DecodeException;
import cn.edu.sustech.cs209.chatting.common.packets.exceptions.EncodeException;

import java.nio.ByteBuffer;
import java.util.UUID;

public class AckPacket extends BasePacket {

  private UUID mid;
  public AckPacket() {
    super(PacketType.ACK);
  }

  public AckPacket(UUID uuid) {
    super(PacketType.ACK);
    mid = uuid;
  }

  public UUID getMid() {
    return mid;
  }

  @Override
  protected ByteBuffer encode() throws EncodeException {
    ByteBuffer buf = ByteBuffer.allocate(16);
    Utils.writeUUID(mid, buf);
    return buf;
  }

  @Override
  protected void decode(ByteBuffer buffer) throws DecodeException {
    mid = Utils.readUUID(buffer);
  }
}
