package net.nel.il.parentassistant.Messaging;

import net.nel.il.parentassistant.model.Message;
import net.nel.il.parentassistant.model.OutputAccount;

import java.util.ArrayList;
import java.util.List;

public class Messaging {

    private final Object messageBlock;

    private List<String> outerMessages;

    private List<String> innerMessages;

    private List<Message> messages;

    private int lastElement = 0;

    public Messaging(){
        messageBlock = new Object();
        outerMessages = new ArrayList<>();
        innerMessages = new ArrayList<>();
        messages = new ArrayList<>();
    }

    public void clearMessages(){
        synchronized (messageBlock){
            messages.clear();
            innerMessages.clear();
            outerMessages.clear();
            lastElement = 0;
        }
    }

    public int getOuterMessagesAmount(){
        synchronized (messageBlock) {
            return outerMessages.size();
        }
    }

    public void addOuterMessages(OutputAccount outputAccount){
        synchronized (messageBlock) {
            System.out.print("addOuterMessages ");
            for(String msg : outputAccount.getMessages()){
                System.out.print(msg + " ");
            }
            System.out.println();
            outerMessages.addAll(outputAccount.getMessages());
            for(String message : outputAccount.getMessages()){
                messages.add(new Message(message, false, System.currentTimeMillis()));
            }
        }
    }

    public int getNewOuterMessagesAmount(){
        int result;
        synchronized (messageBlock){
            result = outerMessages.size() - lastElement;
        }
        return result;
    }

    @SuppressWarnings("all")
    public List<String> getOuterMessages(){
        List<String> msgs = new ArrayList<>();
        synchronized (messageBlock){
            msgs.addAll(outerMessages.subList(lastElement, outerMessages.size()));
            lastElement = outerMessages.size();
        }
        return msgs;
    }

    @SuppressWarnings("all")
    public List<Message> getMessages(){
        List<Message> messagesCopy = new ArrayList<>();
        synchronized (messageBlock){
            getOuterMessages();
            for(Message message : messages){
                messagesCopy.add(message);
            }
        }
        return messagesCopy;
    }


    public int addInnerMessage(String message){
        int position;
        innerMessages.add(message);
        synchronized (messageBlock){
            messages.add(new Message(message, true, System.currentTimeMillis()));
            position = messages.size() - 1;
        }
        return position;
    }

    public void setBadMessage(int position){
        synchronized (messageBlock){
            messages.get(position).setBadState(true);
        }
    }
}
