package com.jccode.mycorrespondence.models;

public class ChatModel {
    String From;
    String Message;
    long timestamp;

    public ChatModel(String from, String message, long timestamp) {
        From = from;
        Message = message;
        this.timestamp = timestamp;
    }

    public String getFrom() {
        return From;
    }

    public String getMessage() {
        return Message;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
