package cn.edu.sustech.cs209.chatting.server.entities;

import cn.edu.sustech.cs209.chatting.server.exceptions.DuplicateGroupNameException;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class User {


  String name;
  Map<User, Conversation> individualConversions;
  Map<Group, Conversation> groupConversions;

  public User (String nam) {
    name = nam;
    individualConversions = new HashMap<>();
    groupConversions = new HashMap<>();
  }
  public String getName() {
    return name;
  }

  public void addIndividualConversion(User user) throws Exception {
    if (individualConversions.containsKey(user)) {
      throw new Exception("conversion already exists");
    }
    individualConversions.put(user, new Conversation());
  }

  public void addGroupConversion(Group group, Conversation conversation) throws DuplicateGroupNameException {
    if (groupConversions.containsKey(group)) {
      throw new DuplicateGroupNameException();
    }
    groupConversions.put(group, conversation);
  }

  /**
   * Get chat list in which conversions are sorted by the last chat time in descending order
   */
  public List<User> getIndividualChatList() {
    return individualConversions.entrySet().stream()
        .sorted(Comparator.comparing((Map.Entry <User, Conversation> e) -> e.getValue().getLastestTimestamp()).reversed())
        .map(e -> e.getKey())
        .toList();
  }

  public List<Group> getGroupChatList() {
    return groupConversions.entrySet().stream()
        .sorted(Comparator.comparing((Map.Entry <Group, Conversation> e) -> e.getValue().getLastestTimestamp()).reversed())
        .map(e -> e.getKey())
        .toList();
  }

  public Conversation getIndividualConversion(User user) {
    return individualConversions.get(user);
  }

  public Conversation getIndividualConversion(Group group) {
    return groupConversions.get(group);
  }
}


