package cn.edu.sustech.cs209.chatting.client.exceptions;

public class SyncException extends Exception{
  private String msg = "";
  public SyncException() {
  }
  public SyncException(String msg) {
    this.msg = msg;
  }

  @Override
  public String getMessage() {
    return msg;
  }
}
