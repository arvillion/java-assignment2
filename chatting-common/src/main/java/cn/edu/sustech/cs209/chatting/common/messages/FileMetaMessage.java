package cn.edu.sustech.cs209.chatting.common.messages;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class FileMetaMessage extends BaseMessage {
  private String fileName;
  private Long fileLength;
  private UUID fileId;

  public FileMetaMessage(UUID uuid, Long timestamp, String sentBy, String sendTo, String fileName, Long fileLength, UUID fileId) {
    super(uuid, timestamp, sentBy, sendTo);

    this.fileName = fileName;
    this.fileLength = fileLength;
    this.fileId = fileId;

  }

  public FileMetaMessage(UUID uuid, Long timestamp, String sentBy, String sendTo) {
    super(uuid, timestamp, sentBy, sendTo);
  }

  @Override
  public ByteBuffer encodeContent() throws IOException {
    ByteArrayOutputStream bs = new ByteArrayOutputStream();
    DataOutputStream ds = new DataOutputStream(bs);
    ds.writeLong(fileLength);
    ds.writeLong(fileId.getMostSignificantBits());
    ds.writeLong(fileId.getLeastSignificantBits());
    ds.writeChars(fileName);
    return ByteBuffer.wrap(bs.toByteArray());
  }

  @Override
  public void decodeContent(ByteBuffer buffer) {
    fileLength = buffer.getLong();
    Long uuidHigh = buffer.getLong();
    Long uuidLow = buffer.getLong();
    fileId = new UUID(uuidHigh, uuidLow);
    byte[] fileNameBuf = new byte[buffer.remaining()];
    buffer.get(fileNameBuf);
    fileName = new String(fileNameBuf, StandardCharsets.UTF_8);
  }
}
