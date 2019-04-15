package io.icode.concareghadmin.application.activities.models;

import com.google.firebase.database.Exclude;

import java.util.List;

public class GroupChats {

    private String sender;
    private List<String> receivers;
    private String message;
    private boolean isseen;

    // unique to identify message to be deleted
    private String key;

    public GroupChats(){}

    public GroupChats(String sender, List<String> receivers, String message, boolean isseen) {
        this.sender = sender;
        this.receivers = receivers;
        this.message = message;
        this.isseen = isseen;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
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
