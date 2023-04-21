package cn.edu.sustech.cs209.chatting.common.packets;

import cn.edu.sustech.cs209.chatting.common.packets.exceptions.DecodeException;
import cn.edu.sustech.cs209.chatting.common.packets.exceptions.EncodeException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class FailPacket extends BasePacket {
  public String getReason() {
    return reason;
  }

  private String reason;

  public FailPacket() {
    super(PacketType.FAIL);
    reason = "";
  }

  public FailPacket(String text) {
    super(PacketType.FAIL);
    reason = text;
  }

  @Override
  protected ByteBuffer encode() throws EncodeException {
    ByteArrayOutputStream bs = new ByteArrayOutputStream();
    if (reason == null || reason.length() == 0) {
      bs.write(0);
    } else {
      byte[] reasonBuf = reason.getBytes(StandardCharsets.US_ASCII);
      if (reasonBuf.length > Byte.MAX_VALUE) {
        throw new EncodeException();
      }
      bs.write(reasonBuf.length);
      try {
        bs.write(reasonBuf);
      } catch (IOException e) {
        throw new EncodeException();
      }
    }
    return ByteBuffer.wrap(bs.toByteArray());
  }

  @Override
  protected void decode(ByteBuffer buffer) throws DecodeException {
    int reasonLen = buffer.get();
    byte[] reasonBuf = new byte[reasonLen];
    buffer.get(reasonBuf);
    reason = new String(reasonBuf);
  }
}
