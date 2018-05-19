package net.nel.il.parentassistant.network;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import net.nel.il.parentassistant.interfaces.MarkerStateListener;
import net.nel.il.parentassistant.model.OutputAccount;

public class NetworkClient implements Runnable {

    public static final int CREATE_ACCOUNT = 1;

    public static final int GET_DATA = 2;

    public static final int UPDATE = 3;

    public static final int REQUEST_TO = 4;

    public static final int ACCEPT_TO = 5;

    public static final int REJECT_TO = 6;

    public static final int BREAK_TO = 10;

    private static final int REQUEST_FROM = 4;

    private static final int ACCEPT_FROM = 5;

    private static final int REJECT_FROM = 6;

    private static final int BREAK_FROM = 10;

    private boolean beginRequestTo = false;

    private final int NO_REQUEST = -1;

    public static int companionId = -1;

    private boolean isRequestFrom = false;

    private int answer = -1;

    private Thread thread;

    private long requestDelay = 4000;

    private Handler handler;

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

    public static final int REMOVE_ROUTE = 10;

    public static final int REBUILD_ROUTE = 11;

    private final int WAITING_TICKS = 10;

    private final int ACCEPTED = 1;

    private final int REJECTED = 0;

    public NetworkClient(Context context, MarkerStateListener markerReceiver,
                         Handler handler) {
        networkClientMessaging = new NetworkClientMessaging(context,
                markerReceiver, handler);
        this.handler = handler;
    }

    public void startNetworkClient() {
        thread = new Thread(this);
        thread.start();
    }

    // do request
    public void setBeginRequestTo(boolean beginRequestTo, int companionId) {
        this.beginRequestTo = beginRequestTo;
        this.companionId = companionId;
    }


    // finish request
    public void setEndRequestTo() {
        beginRequestTo = false;
    }

    // accept request
    public void setAccept() {
        answer = 1;
    }

    // reject request
    public void setReject() {
        answer = 0;
    }

    // finish request from another person
    public void setEndRequestFrom() {
        endRequestFrom = true;
        handler.sendEmptyMessage(REMOVE_ROUTE);
    }


    @Override
    public void run() {
        OutputAccount account;
        while (true) {
            account = networkClientMessaging.sendDataRequest();
            System.out.println("Runnable: id " + account.getId());
            long beginTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - beginTime <= requestDelay) {
                handleRequestTo(account);
                handleRequestFrom(account);
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void handleRequestTo(OutputAccount account) {
        if (beginRequestTo) {
            if (requestAmount == 0) {
                requestTo();
            }
            requestAmount++;
            if (account.getId() == ACCEPT_FROM) {
                handler.sendEmptyMessage(REQUEST_ACCEPTED);
                routingFrom = true;
            }
            if (account.getId() == REJECT_FROM) {
                handler.sendEmptyMessage(REQUEST_REJECTED);
                resetVariablesTo();
            }
            if (account.getId() == BREAK_FROM) {
                handler.sendEmptyMessage(REQUEST_BROKEN);
                handler.sendEmptyMessage(REMOVE_ROUTE);
                resetVariablesTo();
            }
            if (requestAmount > WAITING_TICKS && !routingFrom) {
                handler.sendEmptyMessage(TIME_EXCEED);
                resetVariablesTo();
            }
            if (routingFrom) {
                rebuildRoute(companionId);
            }
        } else {
            if (requestAmount != 0) {
                handler.sendEmptyMessage(REMOVE_ROUTE);
                breakTo();
                resetVariablesTo();
            }
        }
    }

    private void handleRequestFrom(OutputAccount account) {
        if (account.getId() == REQUEST_FROM) {
            isRequestFrom = true;
            if (companionId == NO_REQUEST) {
                doIsRequestFrom(account);
            }
            companionId = account.getCompanionId();
        }
        if (answer == REJECTED) {
            rejectTo();
            resetVariablesFrom();
        }
        if (answer == ACCEPTED) {
            answer = -1;
            routingTo = true;
            acceptTo();
        }
        if (endRequestFrom) {
            endRequestFrom = false;
            breakTo();
            resetVariablesFrom();
        }
        if (routingTo) {
                rebuildRoute(companionId);
        }
        if (account.getId() == BREAK_FROM) {
            if (isRequestFrom) {
                handler.sendEmptyMessage(REQUEST_BROKEN);
                handler.sendEmptyMessage(REMOVE_ROUTE);
            }
            resetVariablesFrom();
        }
    }

    private void rebuildRoute(int companionId){
        Message message = handler.obtainMessage();
        message.what = REBUILD_ROUTE;
        message.arg1 = companionId;
        handler.sendMessage(message);
    }

    private void doIsRequestFrom(OutputAccount account){
        Message message = handler.obtainMessage();
        message.what = IS_REQUEST_FROM;
        message.arg1 = account.getCompanionId();
        handler.sendMessage(message);
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

    private void requestTo() {
        networkClientMessaging.requestTo(REQUEST_TO, companionId);
    }

    private void acceptTo() {
        networkClientMessaging.acceptTo(ACCEPT_TO, companionId);
    }

    private void rejectTo() {
        networkClientMessaging.rejectTo(REJECT_TO, companionId);
    }

    private void breakTo() {
        networkClientMessaging.breakTo(BREAK_TO, companionId);
    }

    public boolean isBeginRequestTo() {
        return beginRequestTo;
    }

    public boolean isEndRequestFrom() {
        return endRequestFrom;
    }
}
