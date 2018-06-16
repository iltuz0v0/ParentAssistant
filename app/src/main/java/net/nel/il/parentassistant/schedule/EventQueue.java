package net.nel.il.parentassistant.schedule;

import net.nel.il.parentassistant.model.OutputAccount;

public class EventQueue {

    private OutputAccount outputAccount;

    private final Object blockingObject;

    public EventQueue(){
        blockingObject = new Object();
        outputAccount = new OutputAccount();
    }

    public OutputAccount getOutputObject(){
        synchronized (blockingObject){
            return new OutputAccount(outputAccount);
        }
    }

    public void setOutputObject(OutputAccount outputObject){
        synchronized (blockingObject){
            outputAccount = new OutputAccount(outputObject);
        }
    }
}
