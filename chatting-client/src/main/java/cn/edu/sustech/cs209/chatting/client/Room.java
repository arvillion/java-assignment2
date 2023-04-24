package cn.edu.sustech.cs209.chatting.client;

import java.util.Date;

public class Room {
  String name;
  Long timestamp;

  public boolean isRead() {
    return isRead;
  }

  public void setRead(boolean read) {
    isRead = read;
  }

  boolean isRead;

  public Room(String name) {
    this.name = name;
    timestamp = new Date().getTime();
    isRead = true;
  }

  public Room(String name, Long timestamp) {
    this.name = name;
    this.timestamp = timestamp;
    isRead = true;
  }

  public void setTimestamp(Long timestamp) {
    this.timestamp = timestamp;
  }

  public Long getTimestamp() {
    return timestamp;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

}
