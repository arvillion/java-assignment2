package cn.edu.sustech.cs209.chatting.common.packets;

public enum PacketType {
  INDIVIDUAL_CHAT_LIST,
  GROUP_CHAT_LIST,
  LOGIN,
  REGISTER,
  OK,
  FAIL,
  MSG_SEND,
  MSG_RECV,
  NEW_GROUP,
  ACK,
  LAST_RECV;

  public static PacketType get(int index) {
    return values()[index];
  }
}
