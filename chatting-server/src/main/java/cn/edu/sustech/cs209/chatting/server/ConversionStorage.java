package cn.edu.sustech.cs209.chatting.server;

import cn.edu.sustech.cs209.chatting.server.entities.Conversation;
import cn.edu.sustech.cs209.chatting.server.entities.Group;
import cn.edu.sustech.cs209.chatting.server.entities.User;
import cn.edu.sustech.cs209.chatting.server.exceptions.ConversionNotFoundException;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ConversionStorage {
  Map<Long, Conversation> conversions = new TreeMap<>();
  private Long hash(String s1, String s2) {
    long result = 0;
    if (s1.compareTo(s2) > 0) {
      String tmp = s1;
      s1 = s2;
      s2 = tmp;
    }
    result += s1.hashCode();
    result += s2.hashCode();
    return result;
  }

  private Long hashTuple(User u1, User u2) {
    return hash("U:" + u1.getName(), "U:" + u2.getName());
  }

  private Long hashGroup(Group group) {
    return (long) ("G:" + group.getName()).hashCode();
  }

  public Conversation newIndividualConversion(User u1, User u2) {
    Long id = hashTuple(u1, u2);
    Conversation conversation = new Conversation();
    u1.addIndividualConversion(u2, conversation);
    u2.addIndividualConversion(u1, conversation);
    conversions.put(id, conversation);
    return conversation;
  }

  public Conversation newGroupConversion(Group group) {
    Long id = hashGroup(group);
    Conversation conversation = new Conversation();
    conversions.put(id, conversation);
    List<User> userList = group.getMembers();
    for (User user : userList) {
      user.addGroupConversion(group, conversation);
    }
    return conversation;
  }

  public Conversation getInvididualConversion(User u1, User u2) throws ConversionNotFoundException {
    Long id = hashTuple(u1, u2);
    if (conversions.get(id) == null) {
      throw new ConversionNotFoundException();
    }
    return conversions.get(id);
  }

  public Conversation getOrNewInvididualConversion(User u1, User u2) {
    Long id = hashTuple(u1, u2);
    if (conversions.get(id) == null) {
      return newIndividualConversion(u1, u2);
    }
    return conversions.get(id);
  }

  public Conversation getGroupConversion(Group group) throws ConversionNotFoundException {
    Long id = hashGroup(group);
    if (conversions.get(id) == null) {
      throw new ConversionNotFoundException();
    }
    return conversions.get(id);
  }
}
