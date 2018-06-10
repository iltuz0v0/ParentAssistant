package net.nel.il.parentassistant.model;

import android.icu.util.GregorianCalendar;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Message{

    private String message;

    private boolean owner;

    private Date time;

    public Message(String message, boolean owner, long time){
        this.message = message;
        this.owner = owner;
        this.time = new Date(time);
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
}
