package cn.edu.sustech.cs209.chatting.server.packets;

import cn.edu.sustech.cs209.chatting.server.packets.exceptions.DecodeException;
import cn.edu.sustech.cs209.chatting.server.packets.exceptions.EncodeException;

import java.nio.ByteBuffer;

public class OKPacket extends BasePacket {

  public OKPacket() {
    type = PacketTypes.OK;
  }
  @Override
  protected ByteBuffer encode() throws EncodeException {
    return ByteBuffer.allocate(0);
  }

  @Override
  protected void decode(ByteBuffer buffer) throws DecodeException {
  }
}
