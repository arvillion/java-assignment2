package cn.edu.sustech.cs209.chatting.common.packets;

import cn.edu.sustech.cs209.chatting.common.packets.*;
import cn.edu.sustech.cs209.chatting.common.packets.exceptions.DecodeException;
import cn.edu.sustech.cs209.chatting.common.packets.exceptions.InvalidPacketException;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class PacketReader {
  InputStream inputStream;
  public PacketReader(InputStream inputStream) {
    this.inputStream = inputStream;
  }

  public BasePacket readPacket() throws IOException, InvalidPacketException, DecodeException {
    int typeNum = inputStream.read();
    PacketType type = PacketType.get(typeNum);
    DataInputStream din = new DataInputStream(inputStream);
    int len = din.readInt();

    if (len < 5) {
      throw new InvalidPacketException();
    }

    byte[] contentBytes = inputStream.readNBytes(len - 5);

    ByteBuffer buf = ByteBuffer.allocate(len);
    buf.put((byte) typeNum);
    buf.putInt(len);
    buf.put(contentBytes);

    switch (type) {
      case LOGIN:
        LoginPacket loginPacket = new LoginPacket();
        loginPacket.decodeFrom(buf);
        return loginPacket;
      case REGISTER:
        RegisterPacket registerPacket = new RegisterPacket();
        registerPacket.decodeFrom(buf);
        return registerPacket;
      case OK:
        OKPacket okPacket = new OKPacket();
        okPacket.decodeFrom(buf);
        return okPacket;
      case FAIL:
        FailPacket failPacket = new FailPacket();
        failPacket.decodeFrom(buf);
        return failPacket;

      default:
        throw new InvalidPacketException();
    }
  }
}