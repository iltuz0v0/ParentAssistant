package net.nel.il.parentassistant.model;

import java.util.Date;

public class Message {

    private String message;

    private boolean owner;

    private Date time;

    private boolean state = false;

    public Message(String message, boolean owner, long time) {
        this.message = message;
        this.owner = owner;
        this.time = new Date(time);
    }

    public Message(String message, boolean owner, long time, boolean state) {
        this.message = message;
        this.owner = owner;
        this.time = new Date(time);
        this.state = state;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isOwner() {
        return owner;
    }

    public void setOwner(boolean owner) {
        this.owner = owner;
    }

    public boolean isState() {
        return state;
    }

    public void setBadState(boolean state) {
        this.state = state;
    }
}
