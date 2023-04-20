package cn.edu.sustech.cs209.chatting.server;

import java.util.List;
import java.util.Map;

public class Server {
  private static Map<String, User> registeredUsers;
  private static Map<String, User> onlineUsers;

  private static Map<String, Group> groups;

  public static User userLogin(String username) throws ServerException {
    username = username.trim();
    if (username.length() == 0) {
      throw new ServerException("username must not be null");
    }
    if (!registeredUsers.containsKey(username)) {
      throw new ServerException("no such a user");
    }
    User user = registeredUsers.get(username);
    onlineUsers.put(username, user);
    return user;
  }

  public static void userRegister(String username) throws ServerException {
    username = username.trim();
    if (username.length() == 0) {
      throw new ServerException("username must not be null");
    }
    if (registeredUsers.containsKey(username)) {
      throw new ServerException("username already exists");
    }
    User user = new User(username);
    registeredUsers.put(username, user);
  }

  public static void userQuit(String username) {
    onlineUsers.remove(username);
  }

  /**
   * Create a new group.
   * A conversion associated with this group is also initialized
   * @param users
   */
  public static void newGroup(String groupName, List<User> users) throws ServerException {
    if (groups.containsKey(groupName)) {
      throw new ServerException("groupName already exists");
    }
    Group group = new Group(users);
    groups.put(groupName, group);
    Conversation conversation = new Conversation();
    try {
      for (User u : users) {
        u.addGroupConversion(group, conversation);
      }
    } catch (Exception e) {
      throw new ServerException(e.getMessage());
    }
  }


}


