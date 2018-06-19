package net.nel.il.parentassistant.network;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;

import net.nel.il.parentassistant.Messaging.Messaging;
import net.nel.il.parentassistant.interfaces.ConnectionStateListener;
import net.nel.il.parentassistant.interfaces.MarkerStateListener;
import net.nel.il.parentassistant.model.ExtraMessage;
import net.nel.il.parentassistant.model.OutputAccount;

import java.util.ArrayList;
import java.util.List;

public class NetworkClient implements Runnable {

    public static final int ACCOUNT_CREATING = 1;

    public static final int GETTING_DATA = 2;

    public static final int UPDATE = 3;

    public static final int REQUEST_TO = 4;

    public static final int ACCEPT_TO = 5;

    public static final int REJECT_TO = 6;

    public static final int BREAK_TO = 10;

    private static final int REQUEST_FROM = 4;

    private static final int ACCEPT_FROM = 5;

    private static final int REJECT_FROM = 6;

    private static final int BREAK_FROM = 10;

    private static final int TIME_OUTSIDE_TO = 11;

    private static final int FIRST_REQUEST_TO = 12;

    public static final int BUSY = 15;

    public static final int BUSY_REQUEST = 17;

    public static final int NO_CONNECTION = 19;

    public static final int CLEAR_MESSAGES = 20;

    public static final int NO_INTERNET = 21;

    public static final int MESSAGE_ANSWER = 30;

    public static final int EVENT_REQUEST = 50;

    public static final int EVENT_CREATION_REQUEST = 51;

    public static final int TRYING_TO_RETURN_OLD_ACCOUNT = 100;

    public static final int NEGATIVE_TRYING_TO_RETURN_OLD_ACCOUNT = 101;

    public static final int POSITIVE_TRYING_TO_RETURN_OLD_ACCOUNT = 102;

    public static final int ACCOUNT_UPLOADED = 103;

    private boolean beginRequestTo = false;

    private final int NO_REQUEST = -1;

    public static int companionId = -1;

    private boolean isRequestFrom = false;

    private int answer = -1;

    private Thread thread;

    private long checkDelay = 1000;

    private long requestDelay = 3000;

    private boolean routingFrom = false;

    private int requestAmount = 0;

    private boolean endRequestFrom = false;

    private boolean routingTo = false;

    private NetworkClientMessaging networkClientMessaging;

    public static final int REQUEST_ACCEPTED = 5;

    public static final int REQUEST_REJECTED = 6;

    public static final int REQUEST_BROKEN = 7;

    public static final int TIME_EXCEED = 8;

    public static final int IS_REQUEST_FROM = 9;

    public static final int ROUTE_REMOVING = 10;

    public static final int ROUTE_REBUILDING = 11;

    private static final int WAITING_TICKS = 9;

    private static final int ACCEPTED = 1;

    private static final int REJECTED = 0;

    public static final int COMPANION_IS_OFFLINE = 13;

    private boolean closed = false;

    private List<ExtraMessage> messages;

    private Messaging messaging;

    private final Object messagingBlock;

    private ConnectionStateListener connectionStateListener;

    private boolean isConnection = true;

    private int type;

    private RequestTo requestTo;

    private RequestFrom requestFrom;

    private boolean accountRequest = false;

    private boolean eventRequest = false;

    private boolean accountNegativeTrying = false;

    private boolean accountPositiveTrying = false;

    private List<LatLng> points;

    private String from;

    private String to;

    private int steps = 0;

    private static final int STEPS_AMOUNT = 3;

    public NetworkClient(Context context, ConnectionStateListener connectionStateListener,
                         MarkerStateListener markerReceiver, Messaging messaging) {
        requestTo = new RequestTo();
        requestFrom = new RequestFrom();
        this.messaging = messaging;
        this.connectionStateListener = connectionStateListener;
        networkClientMessaging = new NetworkClientMessaging(context,
                connectionStateListener, markerReceiver, messaging);
        messagingBlock = new Object();
        messages = new ArrayList<>();
    }

    public void startNetworkClient() {
        thread = new Thread(this);
        thread.start();
    }

    @SuppressWarnings("all")
    public void setBeginRequestTo(boolean beginRequestTo, int companionId, int type) {
        this.beginRequestTo = beginRequestTo;
        this.companionId = companionId;
        this.type = type;
    }

    public void setAccountRequest(List<LatLng> points, String from, String to){
        this.points = points;
        this.from = from;
        this.to = to;
        accountRequest = true;
    }

    public void setEventRequest(List<LatLng> points, String from, String to){
        this.points = points;
        this.from = from;
        this.to = to;
        eventRequest = true;
    }


    public void setEndRequestTo() {
        beginRequestTo = false;
    }

    public void setAccept() {
        answer = ACCEPTED;
    }

    public void setReject() {
        answer = REJECTED;
    }

    public void setReject(boolean isConnection) {
        this.isConnection = isConnection;
        answer = REJECTED;
    }

    public void setEndRequestFrom() {
        endRequestFrom = true;
        connectionStateListener.redirectRemovingRoute(ROUTE_REMOVING);
    }


    @Override
    @SuppressWarnings("all")
    public void run() {
        boolean firstRequest =  false;
        OutputAccount account;
        while (true) {
            handleMessaging();
            if(!firstRequest){
                networkClientMessaging.sendFirstRequest(FIRST_REQUEST_TO);
                firstRequest = true;
            }
            finishRequest();
            account = networkClientMessaging.sendDataRequest();
            long beginTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - beginTime <= checkDelay) {
                doTryingToReturnOldAccount(account);
                specialEvents();
                finishRequest();
                handleRequestTo(account);
                finishRequest();
                handleRequestFrom(account);
                try {
                    Thread.sleep(requestDelay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void doTryingToReturnOldAccount(OutputAccount account){
        if(account.getId() == TRYING_TO_RETURN_OLD_ACCOUNT){
            connectionStateListener.redirectAccountTrying(TRYING_TO_RETURN_OLD_ACCOUNT);
        }
        if(accountNegativeTrying){
            accountNegativeTrying = false;
            sendAccountTryingNegative();
        }
        if(accountPositiveTrying){
            accountPositiveTrying = false;
            sendAccountTryingPositive();
        }
    }

    public void doAlertNegative(){
        accountNegativeTrying = true;
    }

    public void doAlertPositive(){
        accountPositiveTrying = true;
    }

    private void specialEvents(){
        if(accountRequest){
            accountRequest = false;
            sendAccountRequest();
        }
        if(eventRequest){
            eventRequest = false;
            sendEventRequest();
        }
    }

    private void handleRequestTo(OutputAccount account) {
        if (beginRequestTo) {
            requestTo.doRequestTo();
            requestTo.acceptFrom(account);
            requestTo.rejectFrom(account);
            requestTo.doNoConnection(account);
            requestTo.breakFrom(account);
            requestTo.doBusyFrom(account);
            requestTo.timeExceed();
            requestTo.rebuildRoute(account);
        } else {
            requestTo.breakTo();
        }
    }

    private void handleRequestFrom(OutputAccount account) {
        requestFrom.doRequestFrom(account);
        requestFrom.rejectTo();
        requestFrom.acceptTo();
        requestFrom.breakTo();
        requestFrom.rebuildRoute(account);
        requestFrom.breakFrom(account);
    }

    private class RequestTo{

        void doRequestTo(){
            if (requestAmount == 0) {
                networkClientMessaging.requestTo(REQUEST_TO, companionId, type);
            }
            requestAmount++;
        }

        void acceptFrom(OutputAccount account){
            if (account.getId() == ACCEPT_FROM) {
                connectionStateListener.redirectOperation(REQUEST_ACCEPTED);
                routingFrom = true;
            }
        }

        void rejectFrom(OutputAccount account){
            if (account.getId() == REJECT_FROM) {
                connectionStateListener.redirectOperation(REQUEST_REJECTED);
                resetVariablesTo();
            }
        }

        void doNoConnection(OutputAccount account){
            if(account.getId() == NO_CONNECTION){
                connectionStateListener.redirectOperation(NO_CONNECTION);
                resetVariablesTo();
            }
        }

        void breakFrom(OutputAccount account){
            if (account.getId() == BREAK_FROM) {
                connectionStateListener.redirectOperation(REQUEST_BROKEN);
                connectionStateListener.redirectRemovingRoute(ROUTE_REMOVING);
                resetVariablesTo();
            }
        }

        void doBusyFrom(OutputAccount account){
            if(account.getId() == BUSY){
                connectionStateListener.redirectOperation(BUSY_REQUEST);
                resetVariablesTo();
            }
        }

        void timeExceed(){
            if (requestAmount > WAITING_TICKS && !routingFrom) {
                connectionStateListener.redirectOperation(TIME_EXCEED);
                networkClientMessaging.timeExceed(TIME_OUTSIDE_TO, companionId);
                resetVariablesTo();
            }
        }

        void rebuildRoute(OutputAccount account){
            if (routingFrom) {
                if(account.getId() != BREAK_FROM) {
                    steps++;
                    if(steps == STEPS_AMOUNT){
                        steps = 0;
                        connectionStateListener.redirectRebuildingRoute(ROUTE_REBUILDING,
                                companionId);
                    }

                }
            }
        }

        void breakTo(){
            if (requestAmount != 0) {
                connectionStateListener.redirectRemovingRoute(ROUTE_REMOVING);
                networkClientMessaging.breakTo(BREAK_TO, companionId);
                resetVariablesTo();
            }
        }
    }

    private class RequestFrom{

        void doRequestFrom(OutputAccount account){
            if (account.getId() == REQUEST_FROM) {
                isRequestFrom = true;
                if (companionId == NO_REQUEST) {
                    connectionStateListener.redirectIsRequestFrom(IS_REQUEST_FROM,
                            account.getCompanionId(), account.getType());
                }
                companionId = account.getCompanionId();
            }
        }

        void rejectTo(){
            if (answer == REJECTED && !isConnection) {
                isConnection = true;
                networkClientMessaging.doNoConnectionTo(NO_CONNECTION, companionId);
                resetVariablesFrom();
            }
            else if (answer == REJECTED) {
                networkClientMessaging.rejectTo(REJECT_TO, companionId);
                resetVariablesFrom();
            }
        }

        void acceptTo(){
            if (answer == ACCEPTED) {
                answer = -1;
                routingTo = true;
                networkClientMessaging.acceptTo(ACCEPT_TO, companionId);
            }
        }

        void breakTo(){
            if (endRequestFrom) {
                endRequestFrom = false;
                networkClientMessaging.breakTo(BREAK_TO, companionId);
                resetVariablesFrom();
            }
        }

        void rebuildRoute(OutputAccount account){
            if (routingTo) {
                if(account.getId() != BREAK_FROM) {
                    steps++;
                    if(steps == STEPS_AMOUNT) {
                        steps = 0;
                        connectionStateListener.redirectRebuildingRoute(ROUTE_REBUILDING,
                                companionId);
                    }
                }
            }
        }

        void breakFrom(OutputAccount account){
            if (account.getId() == BREAK_FROM) {
                if (isRequestFrom) {
                    connectionStateListener.redirectOperation(REQUEST_BROKEN);
                    connectionStateListener.redirectRemovingRoute(ROUTE_REMOVING);
                }
                resetVariablesFrom();
            }
        }
    }

    private void resetVariablesTo() {
        beginRequestTo = false;
        requestAmount = 0;
        routingFrom = false;
        companionId = NO_REQUEST;
    }

    private void resetVariablesFrom() {
        routingTo = false;
        answer = -1;
        companionId = NO_REQUEST;
        isRequestFrom = false;
    }

    private void sendAccountRequest(){
        OutputAccount outputAccount = networkClientMessaging.sendAccountRequest(EVENT_REQUEST, points, from,
                to);
        if(outputAccount.getId() == EVENT_REQUEST) {
            connectionStateListener.redirectOutputObject(EVENT_REQUEST, outputAccount);
        }
    }

    private void sendAccountTryingNegative(){
        networkClientMessaging
                .doAlertNegative(NEGATIVE_TRYING_TO_RETURN_OLD_ACCOUNT);
    }

    private void sendAccountTryingPositive(){
        boolean result = networkClientMessaging
                .doAlertPositive(POSITIVE_TRYING_TO_RETURN_OLD_ACCOUNT);
        connectionStateListener.redirectOperation(ACCOUNT_UPLOADED, result);
    }

    private void sendEventRequest(){
        networkClientMessaging.sendAccountRequest(EVENT_CREATION_REQUEST, points,
                from, to);
    }

    public boolean isBeginRequestTo() {
        return beginRequestTo;
    }

    public boolean isEndRequestFrom() {
        return endRequestFrom;
    }

    private void setClose(){
        networkClientMessaging.timeExceed(TIME_OUTSIDE_TO, companionId);
        resetVariablesTo();
        connectionStateListener.redirectRemovingRoute(ROUTE_REMOVING);
    }

    public void close(){
        closed = true;
    }

    private void finishRequest(){
        if(closed){
            setClose();
            closed = false;
        }
    }

    public void setSendMessage(String message, int position, int finishPosition){
        synchronized (messagingBlock) {
            messages.add(new ExtraMessage(message, position, finishPosition));
        }
    }

    @SuppressWarnings("all")
    private void handleMessaging(){
        List<ExtraMessage> msgs = null;
        synchronized (messagingBlock) {
            if (messages.size() > 0) {
                msgs = new ArrayList<>();
                msgs.addAll(messages);
                messages.clear();
            }
        }
        if(msgs != null) {
            for (int element = 0; element < msgs.size(); element++) {
                String result = networkClientMessaging.sendMessage(
                        msgs.get(element).getMessage(), companionId);
                if(result == NetworkClientMessaging.DEFAULT_OUTPUT_JSON){
                    connectionStateListener.redirectMessageAnswer(MESSAGE_ANSWER,
                            msgs.get(element).getPosition(), msgs.get(element).getFinishPosition());
                }
            }
        }
    }
}
