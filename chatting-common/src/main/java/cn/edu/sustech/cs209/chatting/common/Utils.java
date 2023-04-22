package cn.edu.sustech.cs209.chatting.common;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.UUID;

public class Utils {
  /**
   * Read a string from byteBuffer and the length of the string
   * is indicated by the first byte read from the buffer
   */
  public static String readShortString(ByteBuffer buf, Charset charset) {
    int len = buf.get();
    byte[] bytes = new byte[len];
    buf.get(bytes);
    return new String(bytes, charset);
  }

  public static String readLongString(ByteBuffer buf, Charset charset) {
    int len = buf.getInt();
    byte[] bytes = new byte[len];
    buf.get(bytes);
    return new String(bytes, charset);
  }

  /**
   * Write a string and its length to a byte stream
   * length is represented by a byte
   */
  public static void writeShortString(String s, OutputStream os, Charset charset) throws IOException {
    byte[] bytes = s.getBytes(charset);
    if (bytes.length > Byte.MAX_VALUE) {
      throw new IOException();
    }
    os.write(bytes.length);
    os.write(bytes);
  }

  /**
   * Write a string and its length to a byte stream
   * length is represented by four byte
   */
  public static void writeLongString(String s, OutputStream os, Charset charset) throws IOException {
    byte[] bytes = s.getBytes(charset);
    if (bytes.length > Integer.MAX_VALUE) {
      throw new IOException();
    }
    DataOutputStream dos = new DataOutputStream(os);
    dos.writeInt(bytes.length);
    os.write(bytes);
  }

  public static void writeUUID(UUID uuid, OutputStream os) throws IOException {
    DataOutputStream ds = new DataOutputStream(os);
    ds.writeLong(uuid.getMostSignificantBits());
    ds.writeLong(uuid.getLeastSignificantBits());
  }

  public static void writeUUID(UUID uuid, ByteBuffer buffer) {
    buffer.putLong(uuid.getMostSignificantBits());
    buffer.putLong(uuid.getLeastSignificantBits());
  }

  public static UUID readUUID(ByteBuffer buffer) {
    Long ms = buffer.getLong();
    Long ls = buffer.getLong();
    return new UUID(ms, ls);
  }
}
