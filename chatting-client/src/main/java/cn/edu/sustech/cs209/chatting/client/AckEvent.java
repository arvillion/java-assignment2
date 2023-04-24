package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.messages.BaseMessage;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;

public class AckEvent extends Event {
  public static final EventType<AckEvent> type = new EventType<>(Event.ANY, "RELAX");
  BaseMessage baseMessage;
  public AckEvent() {
    super(type);
//    this.baseMessage = baseMessage;
  }


}
