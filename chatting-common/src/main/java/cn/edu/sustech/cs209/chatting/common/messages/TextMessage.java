package cn.edu.sustech.cs209.chatting.common.messages;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class TextMessage extends BaseMessage{
  private String text;

  public TextMessage(Long timestamp, String sentBy, String sendTo, String text) {
    super(timestamp, sentBy, sendTo);
    this.text = text;
  }

  public TextMessage(Long timestamp, String sentBy, String sendTo) {
    super(timestamp, sentBy, sendTo);
  }

  @Override
  public ByteBuffer encodeContent() {
    return ByteBuffer.wrap(text.getBytes());
  }

  @Override
  public void decodeContent(ByteBuffer buffer) {
    byte[] bytes = new byte[buffer.remaining()];
    buffer.get(bytes);
    text = new String(bytes, StandardCharsets.UTF_8);
  }

  public String getText() {
    return text;
  }
}
