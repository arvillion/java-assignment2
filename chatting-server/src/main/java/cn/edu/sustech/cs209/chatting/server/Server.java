package cn.edu.sustech.cs209.chatting.server;

import cn.edu.sustech.cs209.chatting.common.messages.BaseMessage;
import cn.edu.sustech.cs209.chatting.common.messages.TextMessage;
import cn.edu.sustech.cs209.chatting.server.entities.Conversation;
import cn.edu.sustech.cs209.chatting.server.entities.Group;
import cn.edu.sustech.cs209.chatting.server.entities.User;
import cn.edu.sustech.cs209.chatting.server.exceptions.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server implements Runnable {
  private static Map<String, User> registeredUsers = new HashMap<>();
  private static Map<String, User> onlineUsers = new HashMap<>();
  private static ConversionStorage conversionStorage = new ConversionStorage();


  private static Map<String, Group> groups = new HashMap<>();
  private static Map<UUID, BaseMessage> messages = new HashMap<>();
  private static Map<User, ClientHandler> clientHandlerMap = new HashMap<>();
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
    clientHandlerMap.put(user, clientHandler);
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
    clientHandlerMap.remove(registeredUsers.get(username));
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

  public static void newTextMessage(User user, TextMessage textMessage) throws InvalidInputException, ServerException {
    String sendTo = textMessage.getSendTo();
    String sendBy = textMessage.getSentBy();
    if (sendTo.startsWith("U:")) {
      sendTo = sendTo.substring(2);
      User receiver = registeredUsers.get(sendTo);
      if (receiver == null) {
        throw new InvalidInputException("Unreachable destination");
      }
      Conversation conversation = conversionStorage.getOrNewInvididualConversion(user, receiver);

      conversation.addMessage(textMessage);
      ClientHandler handler = clientHandlerMap.get(receiver);
      if (handler != null) {
        handler.sendMessage(textMessage);
      }

    } else if (sendTo.startsWith("G:")) {
      sendTo = sendTo.substring(2);
      Group receiver = groups.get(sendTo);
      if (receiver == null) {
        throw new InvalidInputException("Unreachable destination");
      }
      try {
        Conversation conversation = conversionStorage.getGroupConversion(receiver);
        conversation.addMessage(textMessage);
      } catch (ConversionNotFoundException e) {
        throw new ServerException();
      }

      List<User> groupMembers = receiver.getMembers();
      for(User member : groupMembers) {
        if (member == user) {
          continue;
        }
        ClientHandler handler = clientHandlerMap.get(member);
        if (handler != null) {
          handler.sendMessage(textMessage);
        }
      }

    } else {
      throw new InvalidInputException("Wrong sendTo format");
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


