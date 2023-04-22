package cn.edu.sustech.cs209.chatting.common.packets;

import cn.edu.sustech.cs209.chatting.common.Utils;
import cn.edu.sustech.cs209.chatting.common.messages.BaseMessage;
import cn.edu.sustech.cs209.chatting.common.messages.FileMetaMessage;
import cn.edu.sustech.cs209.chatting.common.messages.MessageType;
import cn.edu.sustech.cs209.chatting.common.messages.TextMessage;
import cn.edu.sustech.cs209.chatting.common.packets.exceptions.DecodeException;
import cn.edu.sustech.cs209.chatting.common.packets.exceptions.EncodeException;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class SendMessagePacket extends BasePacket{

  private BaseMessage baseMessage;
//  private MessageType messageType;


  public SendMessagePacket(TextMessage textMessage) {
    super(PacketType.MSG_SEND);
    this.baseMessage = textMessage;
//    messageType = MessageType.TEXT;
  }

  public SendMessagePacket(FileMetaMessage fileMetaMessage) {
    super(PacketType.MSG_SEND);
    this.baseMessage = fileMetaMessage;
//    messageType = MessageType.FILE_META;
  }

  public SendMessagePacket() {
    super(PacketType.MSG_SEND);

  }

  @Override
  protected ByteBuffer encode() throws EncodeException {
    ByteArrayOutputStream bs = new ByteArrayOutputStream();
    DataOutputStream ds = new DataOutputStream(bs);
    try {
      ds.writeByte(baseMessage.getMessageType().ordinal());
      ds.writeLong(baseMessage.getTimestamp());
      Utils.writeUUID(baseMessage.getUuid(), ds);

      String sentBy = baseMessage.getSentBy();
      String sentTo = baseMessage.getSendTo();

      Utils.writeShortString(sentBy, ds, StandardCharsets.UTF_8);
      Utils.writeShortString(sentTo, ds, StandardCharsets.UTF_8);

      ds.write(baseMessage.encodeContent().array());
    } catch (Exception e) {
      throw new EncodeException();
    }
    return ByteBuffer.wrap(bs.toByteArray());
  }

  @Override
  protected void decode(ByteBuffer buffer) throws DecodeException {
    MessageType type = MessageType.get(buffer.get());
    Long timestamp = buffer.getLong();
    UUID uuid = Utils.readUUID(buffer);
    String sentBy = Utils.readShortString(buffer, StandardCharsets.UTF_8);
    String sentTo = Utils.readShortString(buffer, StandardCharsets.UTF_8);
    if (type == MessageType.TEXT) {
      TextMessage textMessage = new TextMessage(uuid, timestamp, sentBy, sentTo);
      textMessage.decodeContent(buffer);
      baseMessage = textMessage;
    } else {
      FileMetaMessage fileMetaMessage = new FileMetaMessage(uuid, timestamp, sentBy, sentTo);
      fileMetaMessage.decodeContent(buffer);
      baseMessage = fileMetaMessage;
    }
  }

  public BaseMessage getBaseMessage() {
    return baseMessage;
  }

//  public MessageType getMessageType() {
//    return messageType;
//  }
}
