package cn.edu.sustech.cs209.chatting.server.packets;

import cn.edu.sustech.cs209.chatting.server.packets.exceptions.DecodeException;
import cn.edu.sustech.cs209.chatting.server.packets.exceptions.EncodeException;

import java.nio.ByteBuffer;

enum PacketTypes{
  INDIVIDUAL_CHAT_LIST,
  GROUP_CHAT_LIST,
  LOGIN,
  REGISTER,
  OK,
  FAIL
}

public abstract class BasePacket {
  PacketTypes type;
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
