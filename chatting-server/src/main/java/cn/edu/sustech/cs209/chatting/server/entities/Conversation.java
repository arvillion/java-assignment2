package cn.edu.sustech.cs209.chatting.server.entities;

import cn.edu.sustech.cs209.chatting.common.messages.BaseMessage;

import java.util.ArrayList;
import java.util.List;

public class Conversation {
  private List<BaseMessage> messages;

  public Conversation() {
    messages = new ArrayList<>();
  }

  public void addMessage(BaseMessage m) {
    messages.add(m);
  }

  /**
   * Get messages sorted by timestamp in ascending order
   */
  public List<BaseMessage> getMessages() {
    return messages;
  }

  public Long getLastestTimestamp() {
    return messages.get(messages.size()-1).getTimestamp();
  }
}
