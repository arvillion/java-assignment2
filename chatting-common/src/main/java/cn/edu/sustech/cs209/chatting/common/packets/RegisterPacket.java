package cn.edu.sustech.cs209.chatting.common.packets;

import cn.edu.sustech.cs209.chatting.common.packets.exceptions.DecodeException;
import cn.edu.sustech.cs209.chatting.common.packets.exceptions.EncodeException;

import javax.swing.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class RegisterPacket extends BasePacket {
  private String username;
  private String password;

  public RegisterPacket() {
    super(PacketTypes.REGISTER);
  }

  public RegisterPacket(String uname, String upass) {
    super(PacketTypes.REGISTER);
    username = uname;
    password = upass;
  }

  @Override
  protected ByteBuffer encode() throws EncodeException {
    ByteArrayOutputStream bs = new ByteArrayOutputStream();
    byte[] unameByte = username.getBytes(StandardCharsets.UTF_8);
    byte[] passByte = password.getBytes(StandardCharsets.US_ASCII);
    try {
      bs.write(unameByte.length);
      bs.write(unameByte);
      bs.write(passByte.length);
      bs.write(passByte);
    } catch (IOException e) {
      throw new EncodeException();
    }
    return ByteBuffer.wrap(bs.toByteArray());
  }

  @Override
  protected void decode(ByteBuffer buffer) throws DecodeException {
    int unameLen = buffer.get();
    byte[] unameBuf = new byte[unameLen];
    buffer.get(unameBuf);
    int passLen = buffer.get();
    byte[] passBuf = new byte[passLen];
    buffer.get(passBuf);
    username = new String(unameBuf, StandardCharsets.UTF_8);
    password = new String(passBuf, StandardCharsets.US_ASCII);
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }
}
