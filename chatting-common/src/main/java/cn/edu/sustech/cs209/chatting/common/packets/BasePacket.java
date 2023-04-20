package cn.edu.sustech.cs209.chatting.common.packets;

import cn.edu.sustech.cs209.chatting.common.packets.exceptions.DecodeException;
import cn.edu.sustech.cs209.chatting.common.packets.exceptions.EncodeException;

import java.nio.ByteBuffer;

public abstract class BasePacket {
  PacketTypes type;

  BasePacket(PacketTypes type) {
    this.type = type;
  }
  protected abstract ByteBuffer encode() throws EncodeException;
  protected abstract void decode(ByteBuffer buffer) throws DecodeException;

  public ByteBuffer toBytes() throws EncodeException{
    ByteBuffer bodyBuffer = encode();
    ByteBuffer buffer = ByteBuffer.allocate(5 + bodyBuffer.limit());
    buffer.put((byte)type.ordinal());
    buffer.putInt(buffer.capacity());
    buffer.put(bodyBuffer);
    return buffer;
  }

  public void decodeFrom(ByteBuffer buffer) throws DecodeException {
    buffer.clear();
    if (buffer.limit() < 2) {
      throw new DecodeException();
    }
    int typeFromBytes = buffer.get();
    int lengthFromBytes = buffer.getInt();

    if (typeFromBytes != type.ordinal()) {
      throw new DecodeException();
    }
    if (lengthFromBytes != buffer.limit()) {
      throw new DecodeException();
    }
    decode(buffer);
  };
}
