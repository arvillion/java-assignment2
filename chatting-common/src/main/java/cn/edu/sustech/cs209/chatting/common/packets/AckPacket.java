package cn.edu.sustech.cs209.chatting.common.packets;

import cn.edu.sustech.cs209.chatting.common.packets.exceptions.DecodeException;
import cn.edu.sustech.cs209.chatting.common.packets.exceptions.EncodeException;

import java.nio.ByteBuffer;

public class AckPacket extends BasePacket {

  public AckPacket() {
    super(PacketType.ACK);
  }
  @Override
  protected ByteBuffer encode() throws EncodeException {
    return ByteBuffer.allocate(0);
  }

  @Override
  protected void decode(ByteBuffer buffer) throws DecodeException {
  }
}
