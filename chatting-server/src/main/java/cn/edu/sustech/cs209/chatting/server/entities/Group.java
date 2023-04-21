package cn.edu.sustech.cs209.chatting.server.entities;

import java.util.List;

public class Group {
  private String name;
  private List<User> members;

  public Group(String name, List<User> users) {
    this.name = name;
    this.members = users;
  }

  public String getName() {
    return name;
  }

  public List<User> getMembers() {
    return members;
  }
}
