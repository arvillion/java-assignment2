package cn.edu.sustech.cs209.chatting.common.messages;

import java.nio.ByteBuffer;

public abstract class BaseMessage {

    private Long timestamp;

    private String sentBy;

    private String sendTo;

    public BaseMessage(Long timestamp, String sentBy, String sendTo) {
        this.timestamp = timestamp;
        this.sentBy = sentBy;
        this.sendTo = sendTo;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public String getSentBy() {
        return sentBy;
    }

    public String getSendTo() {
        return sendTo;
    }



    public abstract ByteBuffer encodeContent() throws Exception;
    public abstract void decodeContent(ByteBuffer buffer) throws Exception;
}
