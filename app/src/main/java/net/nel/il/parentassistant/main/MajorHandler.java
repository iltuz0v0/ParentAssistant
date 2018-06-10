package net.nel.il.parentassistant.main;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;

import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;

import net.nel.il.parentassistant.Messaging.Messaging;
import net.nel.il.parentassistant.Messaging.MessagingDialog;
import net.nel.il.parentassistant.R;
import net.nel.il.parentassistant.interfaces.ConnectionStateListener;
import net.nel.il.parentassistant.network.NetworkClient;
import net.nel.il.parentassistant.network.NetworkClientMessaging;
import net.nel.il.parentassistant.interfaces.LocationReceiver;
import net.nel.il.parentassistant.interfaces.MarkerStateListener;
import net.nel.il.parentassistant.location.GPSManager;
import net.nel.il.parentassistant.model.InfoAccount;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class MajorHandler implements LocationReceiver,
        MarkerStateListener, ConnectionStateListener {

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

    private MessagingDialog messagingDialog;

    private boolean messagingDialogState = false;

    private boolean isNullHandler = false;

    private final Object blockingObject;

    MajorHandler(Context context, LocationManager locationManager,
                 MapFragment map, Handler mainActivityHandler) {
        blockingObject = new Object();
        variablesInitialization(context, locationManager, map, mainActivityHandler);
    }

    public void beginTracingGPS(Activity context) {
        isGPS = providersService.trackGPS(context, locationManager);
        if (isGPS && MainActivity.isPermissionLocation) {
            runGPSTracing(context);
        }
    }

    public void beginSearching(Activity context) {
        if (providersService.search(context, locationManager)) {
            if (!isRunningNetworkClient) {
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
        mapManager.setAdjustedLocation(lastLocation, context);
    }

    public void changeRadius(Context context) {
        mapManager.changeRadius(context);
    }

    @Override
    public void sendLocation(Location currentLocation) {
        mapManager.adjustLocation(currentLocation);
    }

    public void findCurrentLocation(){
        mapManager.findCurrentLocation();
    }

    public void refreshChat(){
        synchronized (blockingObject) {
            if (mainActivityHandler == null){
                while (isNullHandler) {
                    continue;
                }
            }
            if (messagingDialogState) {
                messagingDialog.refreshChat();
            } else {
                if (messaging.getNewOuterMessagesAmount() == 1) {
                    sendMainActivityHandlerMessage(MainActivity.ONE_MESSAGE_ICON);
                } else if (messaging.getNewOuterMessagesAmount() > 1) {
                    sendMainActivityHandlerMessage(MainActivity.MANY_MESSAGES_ICON);
                }
            }
        }
    }

    public void setMessagingDialogState(boolean state){
        this.messagingDialogState = state;
    }

    public void finishRequest() {
        if (networkClient.isBeginRequestTo()) {
            networkClient.setEndRequestTo();
        }
        else {
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

    public void setClose(){
        mapManager.unblockEverything();
        networkClient.close();
    }

    public void blockEverything(){
        mapManager.blockEverything();
    }

    public void unblockEverything(){
        mapManager.unblockEverything();
    }

    public int illuminateMarker(int id){
        return mapManager.illuminateMarker(id);
    }

    public void buildPossibleRoute(int id){
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
        Message message = mainActivityHandler.obtainMessage();
        message.arg1 = id;
        message.what = IS_REQUEST_TO;
        sendMainActivityHandlerMessage(message);
    }

    public void sendCompanionRequest(int companionId, int type){
        networkClient.setBeginRequestTo(true, companionId, type);
        mapManager.blockEverything();
    }

    private void sendRequestFrom(Message message) {
        Message message2 = mainActivityHandler.obtainMessage();
        message2.arg1 = message.arg1;
        message2.arg2 = message.arg2;
        message2.what = NetworkClient.IS_REQUEST_FROM;
        sendMainActivityHandlerMessage(message2);
    }

    public void openMessagingDialog(Activity context){
        context.getFragmentManager().beginTransaction().add(
                R.id.main_layout, messagingDialog).addToBackStack(null).commit();
    }

    public void sendMessage(String message){
        networkClient.setSendMessage(message);
    }

    public Messaging getMessagingObject(){
        return messaging;
    }

    private void variablesInitialization(Context context,
            LocationManager locationManager, MapFragment map,
            Handler mainActivityHandler) {
        messaging = new Messaging();
        messagingDialog = new MessagingDialog();
        this.mainActivityHandler = mainActivityHandler;
        this.locationManager = locationManager;
        networkClient = new NetworkClient(context,this,
                this, messaging);
        routeClient = new RouteClient(this);
        mapManager = new MapManager(context,
                this, mainActivityHandler, routeClient);
        mapManager.uploadMap(map);
        providersService = new ProvidersService();
        gpsManager = new GPSManager(context, this,
                locationManager);
    }

    public void refreshMap(MapFragment map){
        mapManager.uploadMap(map);
    }

    public void clearMessages(){
        messaging.clearMessages();
    }


    @SuppressWarnings("all")
    public void sendMainActivityHandlerMessage(int what){
        synchronized (blockingObject){
            if (mainActivityHandler == null){
                while (isNullHandler) {
                    continue;
                }
            }
            mainActivityHandler.sendEmptyMessage(what);
        }
    }


    @SuppressWarnings("all")
    public void sendMainActivityHandlerMessage(Message message){
        synchronized (blockingObject) {
            if (mainActivityHandler == null){
                while (isNullHandler) {
                    continue;
                }
            }
            mainActivityHandler.sendMessage(message);
        }
    }

    public void nullHandler(){
        synchronized (blockingObject){
            mainActivityHandler = null;
            isNullHandler = true;
        }
    }

    public void changeMarkerColorToDefault(int companionId){
        mapManager.changeMarkerColorToDefault(companionId);
    }

    public void changeMarkerColorToSpecial(int companionId){
        mapManager.changeMarkerColorToSpecial(companionId);
    }


    public void recreateHandler(Handler mainActivityHandler){
        mapManager.setHandler(mainActivityHandler);
        this.mainActivityHandler = mainActivityHandler;
        isNullHandler = false;
    }

    public void refreshChatState(MessagingDialog messagingDialog){
        this.messagingDialog = messagingDialog;
    }

    @Override
    public void redirectOperation(int code) {
        sendMainActivityHandlerMessage(code);
    }

    @Override
    public void redirectRemovingRoute(int code) {
        Message message = mainActivityHandler.obtainMessage();
        message.what = NetworkClient.ROUTE_REMOVING;
        sendMainActivityHandlerMessage(message);
    }

    public void removeRoute(){
        mapManager.removeRoute();
    }

    @Override
    public void redirectRebuildingRoute(int code, int companionId) {
        Message message = mainActivityHandler.obtainMessage();
        message.what = NetworkClient.ROUTE_REBUILDING;
        message.arg1 = companionId;
        sendMainActivityHandlerMessage(message);
    }

    public void rebuildRoute(int companionId){
        mapManager.rebuildRoute(companionId);
    }

    @Override
    public void redirectIsRequestFrom(int code, int companionId, int type) {
        Message message = mainActivityHandler.obtainMessage();
        message.what = code;
        message.arg1 = companionId;
        message.arg2 = type;
        sendRequestFrom(message);
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
        Message message = mainActivityHandler.obtainMessage();
        message.what = code;
        message.obj = infoAccount;
        sendMainActivityHandlerMessage(message);
    }

    @Override
    public void redirectMarkerAddition(int code, InfoAccount infoAccount) {
        Message message = mainActivityHandler.obtainMessage();
        message.what = code;
        message.obj = infoAccount;
        sendMainActivityHandlerMessage(message);
    }

    @Override
    public void redirectMarkerDeleting(int code, Set<Integer> markers) {
        Message message = mainActivityHandler.obtainMessage();
        message.what = code;
        message.obj = markers;
        sendMainActivityHandlerMessage(message);
    }

    @Override
    public void redirectRouteDrawing(int code, List<List<HashMap<String, String>>> route) {
        Message message = mainActivityHandler.obtainMessage();
        message.what = code;
        message.obj = route;
        sendMainActivityHandlerMessage(message);
    }

    public void doCompanionRequest(int code, int companionId){
        trySendCompanionRequest(companionId);
    }

}