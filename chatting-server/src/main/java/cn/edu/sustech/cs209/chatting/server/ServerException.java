package cn.edu.sustech.cs209.chatting.server;

public class ServerException extends Exception{
  String errMsg;
  public ServerException(String err) {
    super();
    errMsg = err;
  }

  @Override
  public String getMessage() {
    return errMsg;
  }
}