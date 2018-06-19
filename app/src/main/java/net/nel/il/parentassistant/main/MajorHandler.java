package net.nel.il.parentassistant.main;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Message;

import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;

import net.nel.il.parentassistant.Messaging.Messaging;
import net.nel.il.parentassistant.Messaging.MessagingFragment;
import net.nel.il.parentassistant.R;
import net.nel.il.parentassistant.ToastManager;
import net.nel.il.parentassistant.interfaces.ConnectionStateListener;
import net.nel.il.parentassistant.interfaces.LocationReceiver;
import net.nel.il.parentassistant.interfaces.MarkerStateListener;
import net.nel.il.parentassistant.location.GPSManager;
import net.nel.il.parentassistant.model.InfoAccount;
import net.nel.il.parentassistant.model.OutputAccount;
import net.nel.il.parentassistant.network.NetworkClient;
import net.nel.il.parentassistant.schedule.EventQueue;
import net.nel.il.parentassistant.schedule.ScheduleFragment;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class MajorHandler implements LocationReceiver, MarkerStateListener, ConnectionStateListener {

    private ProvidersService providersService;

    private GPSManager gpsManager;

    private LocationManager locationManager;

    private MapManager mapManager;

    private RouteClient routeClient;

    private NetworkClient networkClient;

    private boolean isGPS = false;

    private boolean isRunningNetworkClient = false;

    private Handler mainActivityHandler;

    public static final int IS_REQUEST_TO = 31;

    public static final int REFRESH_CHAT = 32;

    private Messaging messaging;

    private EventQueue eventQueue;

    private MessagingFragment messagingFragment;

    private ScheduleFragment scheduleFragment;

    private boolean messagingDialogState = false;

    private boolean scheduleFragmentState = false;

    private boolean isNullHandler = false;

    private final Object blockingObject;

    private boolean isStarted = false;

    private boolean isAccountTrying = false;

    private boolean isConnection = false;

    MajorHandler(Context context, LocationManager locationManager, MapFragment map, Handler mainActivityHandler) {
        blockingObject = new Object();
        variablesInitialization(context, locationManager, map, mainActivityHandler);
    }


    public void beginTrackingGPS(MainActivity context, boolean isPermission) {
        context.checkPermissions();
        isGPS = providersService.trackGPS(context, locationManager, isPermission);
        if (isGPS && isPermission) {
            if(!isConnection) {
                runGPSTracing(context);
                isConnection = true;
            }
        }
    }

    public void beginSearching(MainActivity context, boolean isPermission) {
        context.checkPermissions();
        if (providersService.search(context, locationManager, isPermission)) {
            if (!isRunningNetworkClient) {
                isStarted = true;
                networkClient.startNetworkClient();
                isRunningNetworkClient = !isRunningNetworkClient;
            }
        }
    }

    @SuppressWarnings("all")
    public void drawRoute(Object routes) {
        mapManager.drawRoute((List<List<HashMap<String, String>>>) routes);
    }

    private void runGPSTracing(Context context) {
        Location lastLocation = gpsManager.findLocation(context, this);
        if(lastLocation != null) {
            mapManager.setAdjustedLocation(lastLocation, context);
        }
    }

    public void changeRadius(Context context) {
        mapManager.changeRadius(context);
    }

    @Override
    public void sendLocation(Location currentLocation) {
        mapManager.adjustLocation(currentLocation);
    }

    public void findCurrentLocation() {
        mapManager.findCurrentLocation();
    }

    public void refreshChat() {
        synchronized (blockingObject) {
            if (mainActivityHandler == null) {
                while (isNullHandler) {
                    continue;
                }
            }
            if (messagingDialogState) {
                messagingFragment.refreshChat();
            } else {
                if (messaging.getNewOuterMessagesAmount() == 1) {
                    sendMainActivityHandlerMessage(MainActivity.ONE_MESSAGE_ICON);
                } else if (messaging.getNewOuterMessagesAmount() > 1) {
                    sendMainActivityHandlerMessage(MainActivity.MANY_MESSAGES_ICON);
                }
            }
        }
    }

    public void setMessagingDialogState(boolean state, MessagingFragment messagingFragment) {
        this.messagingFragment = messagingFragment;
        this.messagingDialogState = state;
    }

    public void setMessagingDialogState(boolean state) {
        this.messagingDialogState = state;
    }

    public void finishRequest() {
        if (networkClient.isBeginRequestTo()) {
            networkClient.setEndRequestTo();
        } else {
            if (!networkClient.isEndRequestFrom()) {
                networkClient.setEndRequestFrom();
            }
        }
        mapManager.unblockEverything();
    }

    public void setAccept() {
        networkClient.setAccept();
        mapManager.blockEverything();
    }

    public void setReject() {
        networkClient.setReject();
        mapManager.removeRoute();
        mapManager.unblockEverything();
    }

    public void setNotAvailable() {
        networkClient.setReject(false);
        mapManager.removeRoute();
        mapManager.unblockEverything();
    }

    public void setClose() {
        mapManager.unblockEverything();
        networkClient.close();
    }

    public void blockEverything() {
        mapManager.blockEverything();
    }

    public void unblockEverything() {
        mapManager.unblockEverything();
    }

    public int illuminateMarker(int id) {
        return mapManager.illuminateMarker(id);
    }

    public void buildPossibleRoute(int id) {
        mapManager.buildPossibleRoute(id);
    }

    @Override
    public void refreshMarker(InfoAccount infoAccount) {
        mapManager.refreshMarker(infoAccount);
    }

    @Override
    public void addMarker(InfoAccount infoAccount) {
        mapManager.addMarker(infoAccount);
    }

    @Override
    public void deleteMarkers(Set<Integer> markersIdentifiers) {
        mapManager.deleteMarkers(markersIdentifiers);
    }

    @Override
    public Location getLocation() {
        return mapManager.getLocation();
    }

    private void trySendCompanionRequest(int id) {
        sendMainActivityHandlerMessage(IS_REQUEST_TO, id, -1);
    }

    public void sendCompanionRequest(int companionId, int type) {
        networkClient.setBeginRequestTo(true, companionId, type);
        mapManager.blockEverything();
    }

    private void sendRequestFrom(int code, int arg1, int arg2) {
        sendMainActivityHandlerMessage(code, arg1, arg2);
    }

    public void openMessagingDialog(Activity context) {
        context.getFragmentManager().beginTransaction().add(R.id.main_layout, messagingFragment).addToBackStack(null).commit();
    }

    public void openScheduleFragment(Activity context) {
        if (!isStarted) {
            ToastManager.showToast(context.getString(R.string.toast_start_search), context);
        } else {
            context.getFragmentManager().beginTransaction().add(R.id.main_layout, scheduleFragment).addToBackStack(null).commit();
        }
    }

    public void sendMessage(String message, int position, int finishPosition) {
        networkClient.setSendMessage(message, position, finishPosition);
    }

    public Messaging getMessagingObject() {
        return messaging;
    }

    public void sendAccountRequest(List<LatLng> points, String from, String to) {
        networkClient.setAccountRequest(points, from, to);
    }

    public void sendEventRequest(List<LatLng> points, String from, String to) {
        networkClient.setEventRequest(points, from, to);
    }

    private void variablesInitialization(Context context, LocationManager locationManager, MapFragment map, Handler mainActivityHandler) {
        messaging = new Messaging();
        eventQueue = new EventQueue();
        messagingFragment = new MessagingFragment();
        scheduleFragment = new ScheduleFragment();
        this.mainActivityHandler = mainActivityHandler;
        this.locationManager = locationManager;
        networkClient = new NetworkClient(context, this, this, messaging);
        routeClient = new RouteClient(this);
        mapManager = new MapManager(context, this, mainActivityHandler, routeClient);
        mapManager.uploadMap(map);
        providersService = new ProvidersService();
        gpsManager = new GPSManager(context, this, locationManager);
    }

    public void refreshMap(MapFragment map) {
        mapManager.uploadMap(map);
    }

    public void clearMessages() {
        messaging.clearMessages();
    }


    @SuppressWarnings("all")
    private void sendMainActivityHandlerMessage(int what) {
        synchronized (blockingObject) {
            if (mainActivityHandler == null) {
                while (isNullHandler) {
                    continue;
                }
            }
            mainActivityHandler.sendEmptyMessage(what);
        }
    }


    @SuppressWarnings("all")
    private void sendMainActivityHandlerMessage(int what, Object obj) {
        synchronized (blockingObject) {
            if (mainActivityHandler == null) {
                while (isNullHandler) {
                    continue;
                }
            }
            Message message1 = mainActivityHandler.obtainMessage();
            message1.what = what;
            message1.obj = obj;
            mainActivityHandler.sendMessage(message1);
        }
    }

    @SuppressWarnings("all")
    private void sendMainActivityHandlerMessage(int what, int arg1, int arg2) {
        synchronized (blockingObject) {
            if (mainActivityHandler == null) {
                while (isNullHandler) {
                    continue;
                }
            }
            Message message1 = mainActivityHandler.obtainMessage();
            message1.what = what;
            message1.arg1 = arg1;
            message1.arg2 = arg2;
            mainActivityHandler.sendMessage(message1);
        }
    }

    public void nullHandler() {
        synchronized (blockingObject) {
            mainActivityHandler = null;
            isNullHandler = true;
        }
    }

    public void changeMarkerColorToDefault(int companionId) {
        mapManager.changeMarkerColorToDefault(companionId);
    }

    public void changeMarkerColorToSpecial(int companionId) {
        mapManager.changeMarkerColorToSpecial(companionId);
    }


    public void recreateHandler(Handler mainActivityHandler) {
        mapManager.setHandler(mainActivityHandler);
        this.mainActivityHandler = mainActivityHandler;
        isNullHandler = false;
    }

    public void refreshChatState(MessagingFragment messagingFragment) {
        this.messagingFragment = messagingFragment;
    }

    @Override
    public void redirectOperation(int code) {
        sendMainActivityHandlerMessage(code);
    }

    @Override
    public void redirectOperation(int code, boolean result) {
        sendWhatObjMessage(code, result);
    }

    @Override
    public void redirectRemovingRoute(int code) {
        sendMainActivityHandlerMessage(code);
    }

    public void removeRoute() {
        mapManager.removeRoute();
    }

    @Override
    public void redirectRebuildingRoute(int code, int companionId) {
        sendMainActivityHandlerMessage(code, companionId, -1);
    }

    public void rebuildRoute(int companionId) {
        mapManager.rebuildRoute(companionId);
    }

    @Override
    public void redirectIsRequestFrom(int code, int companionId, int type) {
        sendRequestFrom(code, companionId, type);
    }

    @Override
    public void redirectRefreshingChat(int code) {
        sendMainActivityHandlerMessage(REFRESH_CHAT);
    }

    @Override
    public void redirectClearingMessages(int code) {
        clearMessages();
    }

    @Override
    public void redirectMarkerRefreshing(int code, InfoAccount infoAccount) {
        sendWhatObjMessage(code, infoAccount);
    }

    @Override
    public void redirectMarkerAddition(int code, InfoAccount infoAccount) {
        sendWhatObjMessage(code, infoAccount);
    }

    @Override
    public void redirectMarkerDeleting(int code, Set<Integer> markers) {
        sendWhatObjMessage(code, markers);
    }

    public void sendAccountsToScheduleFragment(Object outputAccount) {
        eventQueue.setOutputObject((OutputAccount) outputAccount);
        if (mainActivityHandler == null) {
            while (isNullHandler) {
                continue;
            }
        }
        if (scheduleFragmentState) {
            scheduleFragment.setOutputAccount(true);
        }
    }

    private void sendWhatObjMessage(int what, Object obj) {
        sendMainActivityHandlerMessage(what, obj);
    }

    @Override
    public void redirectRouteDrawing(int code, List<List<HashMap<String, String>>> route) {
        sendWhatObjMessage(code, route);
    }

    public EventQueue getEventQueue() {
        return eventQueue;
    }

    @Override
    public void redirectOutputObject(int code, OutputAccount outputAccount) {
        sendMainActivityHandlerMessage(code, outputAccount);
    }

    @Override
    public void redirectMessageAnswer(int code, int position, int finishPosition) {
        sendMainActivityHandlerMessage(code, position, finishPosition);
    }

    @Override
    public void redirectAccountTrying(int code) {
        if (!isAccountTrying) {
            isAccountTrying = true;
            sendMainActivityHandlerMessage(code);
        }
    }

    public void setNegativeAccountTrying() {
        isAccountTrying = false;
    }

    public void doCompanionRequest(int code, int companionId) {
        trySendCompanionRequest(companionId);
    }

    public void setScheduleFragmentState(boolean state, ScheduleFragment scheduleFragment) {
        this.scheduleFragment = scheduleFragment;
        scheduleFragmentState = state;
    }

    public void sendBadMessageState(int position, int finishPosition) {
        if (mainActivityHandler == null) {
            while (isNullHandler) {
                continue;
            }
        }
        if (messagingDialogState) {
            messagingFragment.setBadMessage(position, finishPosition);
        }
    }

    public void doAlertNegative() {
        networkClient.doAlertNegative();
    }

    public void doAlertPositive() {
        networkClient.doAlertPositive();
    }

}