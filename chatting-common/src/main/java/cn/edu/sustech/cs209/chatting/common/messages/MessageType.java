package cn.edu.sustech.cs209.chatting.common.messages;

public enum MessageType {
  TEXT,
  FILE_META;

  public static MessageType get(int index) {
    return values()[index];
  }
}
