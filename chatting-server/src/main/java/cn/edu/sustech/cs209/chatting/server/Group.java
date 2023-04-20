package cn.edu.sustech.cs209.chatting.server;

import java.util.List;

public class Group {
  private List<User> members;

  public Group(List<User> users) {
    this.members = users;
  }

  public List<User> getMembers() {
    return members;
  }
}
