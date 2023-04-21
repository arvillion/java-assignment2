package cn.edu.sustech.cs209.chatting.common.packets;

import cn.edu.sustech.cs209.chatting.common.packets.exceptions.EncodeException;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class PacketWriter {
  private OutputStream outputStream;
  public PacketWriter(OutputStream outputStream) {
    this.outputStream = outputStream;
  }

  public void write(BasePacket packet) throws EncodeException, IOException {
    ByteBuffer buffer = packet.toBytes();
    outputStream.write(buffer.array());
  }

}
