package cn.edu.sustech.cs209.chatting.client;
import javafx.event.Event;
import javafx.event.EventType;

public class OKEvent extends Event {
  public static final EventType<OKEvent> type = new EventType<>(Event.ANY, "OK");
  public OKEvent() {
    super(type);
  }


}