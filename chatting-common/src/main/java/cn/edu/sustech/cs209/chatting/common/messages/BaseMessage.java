package cn.edu.sustech.cs209.chatting.common.messages;

import java.nio.ByteBuffer;
import java.util.UUID;

public abstract class BaseMessage {

    private Long timestamp;

    private String sentBy;

    private String sendTo;
    private UUID uuid;

    public BaseMessage(UUID uuid, Long timestamp, String sentBy, String sendTo) {
        this.uuid = uuid;
        this.timestamp = timestamp;
        this.sentBy = sentBy;
        this.sendTo = sendTo;
    }

    public UUID getUuid() {
        return uuid;
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
