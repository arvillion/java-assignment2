package cn.edu.sustech.cs209.chatting.server;

import cn.edu.sustech.cs209.chatting.server.exceptions.InvalidInputException;
import cn.edu.sustech.cs209.chatting.server.exceptions.WrongUnameException;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Server implements Runnable {
  private static Map<String, User> registeredUsers = new HashMap<>();
  private static Map<String, User> onlineUsers = new HashMap<>();

  private static Map<String, Group> groups = new HashMap<>();

  private int LISTEN_PORT = 23456;

  public static User userLogin(String username) throws InvalidInputException, WrongUnameException {
    username = username.trim();
    if (username.length() == 0) {
      throw new InvalidInputException("username must not be null");
    }
    if (!registeredUsers.containsKey(username)) {
      throw new WrongUnameException();
    }
    User user = registeredUsers.get(username);
    onlineUsers.put(username, user);
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

        System.out.printf("[%d] Client connected\n", s.getPort());
        Thread t = new Thread(new ClientHandler(s));
        t.start();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}


