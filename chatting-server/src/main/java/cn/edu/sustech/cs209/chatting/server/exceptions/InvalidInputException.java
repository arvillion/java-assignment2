package cn.edu.sustech.cs209.chatting.server.exceptions;

public class InvalidInputException extends Exception{
  private String errMsg;
  public InvalidInputException(String msg) {
    errMsg = msg;
  }

  @Override
  public String getMessage() {
    return errMsg;
  }
}
