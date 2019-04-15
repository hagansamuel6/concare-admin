package io.icode.concareghadmin.application.activities.models;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.List;

public class Chats {

    private String sender;
    private String receiver;
    private List<String> receivers;
    private String message;
    private String timeStamp;
    private boolean isseen;


    // unique to identify message to be deleted
    private String key;

    public Chats(){}

    public Chats(String sender, String receiver, List<String> receivers, String message,String timeStamp, boolean isseen) {
        this.sender = sender;
        this.receiver = receiver;
        this.receivers = receivers;
        this.message = message;
        this.timeStamp = timeStamp;
        this.isseen = isseen;
        //this.receivers = new ArrayList<>();
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public List<String> getReceivers() {
        return receivers;
    }

    public void setReceivers(List<String> receivers) {
        this.receivers = receivers;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public boolean isIsseen() {
        return isseen;
    }

    public void setIsseen(boolean isseen) {
        this.isseen = isseen;
    }

    //getters and setters to store && retrieve the unique key of each message
    // excluding from database field
    @Exclude
    public String getKey() {
        return key;
    }

    @Exclude
    public void setKey(String key) {
        this.key = key;
    }
}
