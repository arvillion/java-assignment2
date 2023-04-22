package cn.edu.sustech.cs209.chatting.server;

import cn.edu.sustech.cs209.chatting.server.entities.Conversation;
import cn.edu.sustech.cs209.chatting.server.entities.Group;
import cn.edu.sustech.cs209.chatting.server.entities.User;
import cn.edu.sustech.cs209.chatting.server.exceptions.DuplicateGroupNameException;
import cn.edu.sustech.cs209.chatting.server.exceptions.InvalidInputException;
import cn.edu.sustech.cs209.chatting.server.exceptions.WrongUnameException;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server implements Runnable {
  private static Map<String, User> registeredUsers = new HashMap<>();
  private static Map<String, User> onlineUsers = new HashMap<>();

  private static Map<String, Group> groups = new HashMap<>();
//  private static Map<String, ClientHandler> clientHandlers = new HashMap<>();

  private int LISTEN_PORT = 23456;

  public static User userLogin(String username, ClientHandler clientHandler) throws InvalidInputException, WrongUnameException {
    username = username.trim();
    if (username.length() == 0) {
      throw new InvalidInputException("username must not be null");
    }
    if (!registeredUsers.containsKey(username)) {
      throw new WrongUnameException();
    }
    User user = registeredUsers.get(username);
    onlineUsers.put(username, user);
//    clientHandlers.put(username, clientHandler);
    return user;
  }

  public static void userRegister(String username) throws InvalidInputException, WrongUnameException {
    username = username.trim();
    if (username.length() == 0) {
      throw new InvalidInputException("username must not be null");
    }
    if (registeredUsers.containsKey(username)) {
      throw new WrongUnameException();
    }
    User user = new User(username);
    registeredUsers.put(username, user);
  }

  public static void userQuit(String username) {
    onlineUsers.remove(username);
//    clientHandlers.remove(username);
  }

  public static List<String> getOnlineUsers() {
    return onlineUsers.keySet().stream().toList();
  }

  public static List<String> getOwnGroups(User user) {
    return user.getGroupChatList().stream().map(g -> g.getName()).toList();
  }

  /**
   * Create a new group.
   * A conversion associated with this group is also initialized
   */
  public static void newGroup(String groupName, List<String> usernames) throws DuplicateGroupNameException, InvalidInputException {
    if (groups.containsKey(groupName)) {
      throw new DuplicateGroupNameException();
    }
    Set<String> uniqueUsernames = new HashSet<>(usernames);
    if (uniqueUsernames.size() < 2) {
      throw new InvalidInputException("Too few members");
    }

    List<User> users = new ArrayList<>();
    for (String username : uniqueUsernames) {
      User u = registeredUsers.get(username);
      if (u == null) {
        throw new InvalidInputException(String.format("User %s does not exist", username));
      }
      users.add(u);
    }
    Group group = new Group(groupName, users);
    groups.put(groupName, group);
    Conversation conversation = new Conversation();

    for (User u : users) {
      u.addGroupConversion(group, conversation);
    }
  }

  public Server() {}
  public Server(int port) {
    LISTEN_PORT = port;
  }

  @Override
  public void run() {

    try {
      ServerSocket server = new ServerSocket(LISTEN_PORT);
      System.out.println("Waiting for clients to connect...");
      while (true) {
        Socket s = server.accept();
        ClientHandler clientHandler = new ClientHandler(s);
        System.out.printf("[%d] Client connected\n", s.getPort());
        Thread t = new Thread(clientHandler);
        t.start();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}


