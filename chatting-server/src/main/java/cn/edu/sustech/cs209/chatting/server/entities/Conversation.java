package cn.edu.sustech.cs209.chatting.server.entities;

import java.util.ArrayList;
import java.util.List;

public class Conversation {
  private List<Message> messages;

  public Conversation() {
    messages = new ArrayList<>();
  }

  public void addMessage(Message m) {
    messages.add(m);
  }

  /**
   * Get messages sorted by timestamp in ascending order
   */
  public List<Message> getMessages() {
    return messages;
  }

  public Long getLastestTimestamp() {
    return messages.get(messages.size()-1).getTimestamp();
  }
}
