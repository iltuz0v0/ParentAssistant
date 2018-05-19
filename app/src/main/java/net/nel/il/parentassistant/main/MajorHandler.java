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

import net.nel.il.parentassistant.network.NetworkClient;
import net.nel.il.parentassistant.network.NetworkClientMessaging;
import net.nel.il.parentassistant.interfaces.LocationReceiver;
import net.nel.il.parentassistant.interfaces.MarkerStateListener;
import net.nel.il.parentassistant.location.GPSManager;
import net.nel.il.parentassistant.model.InfoAccount;

import java.util.HashMap;
import java.util.List;

public class MajorHandler implements LocationReceiver, Handler.Callback,
        MarkerStateListener {

    private ProvidersService providersService;

    private GPSManager gpsManager;

    private LocationManager locationManager;

    private MapManager mapManager;

    private RouteClient routeClient;

    private NetworkClient networkClient;

    private boolean isGPS = false;

    private boolean isRunningNetworkClient = false;

    private Handler handler;

    private Handler mainActivityHandler;

    public static final int IS_REQUEST_TO = 1;

    MajorHandler(Context context, LocationManager locationManager,
                 MapFragment map, LayoutInflater layoutInflater,
                 Handler mainActivityHandler) {
        variablesInitialization(context, locationManager, map,
                layoutInflater, mainActivityHandler);
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
    private void drawRoute(Object routes) {
        mapManager.drawRoute((List<List<HashMap<String, String>>>) routes);
    }

    private void runGPSTracing(Context context) {
        Location lastLocation = gpsManager.findLocation(context);
        mapManager.setAdjustedLocation(lastLocation, context);
    }

    public void changeRadius(Context context) {
        mapManager.changeRadius(context);
    }

    @Override
    public void sendLocation(LatLng currentLocation) {
        mapManager.adjustLocation(currentLocation);
    }

    @Override
    public boolean handleMessage(Message message) {
        switch (message.what) {
            case RouteClient.HANDLER_DRAW_ROUTE:
                drawRoute(message.obj);
                break;
            case NetworkClientMessaging.ADD_MARKER:
                addMarker((InfoAccount) message.obj);
                break;
            case NetworkClientMessaging.REFRESH_MARKER:
                refreshMarker((InfoAccount) message.obj);
                break;
            case NetworkClientMessaging.DELETE_MARKER:
                deleteMarkers((List<Integer>) message.obj);
                break;
            case MapManager.COMPANION_REQUEST:
                sendCompanionRequest(message.arg1);
                break;
            case NetworkClient.REQUEST_ACCEPTED:
                mainActivityHandler.sendEmptyMessage(NetworkClient.REQUEST_ACCEPTED);
                break;
            case NetworkClient.REQUEST_REJECTED:
                mainActivityHandler.sendEmptyMessage(NetworkClient.REQUEST_REJECTED);
                break;
            case NetworkClient.REQUEST_BROKEN:
                mainActivityHandler.sendEmptyMessage(NetworkClient.REQUEST_BROKEN);
                break;
            case NetworkClient.TIME_EXCEED:
                mainActivityHandler.sendEmptyMessage(NetworkClient.TIME_EXCEED);
                break;
            case NetworkClient.IS_REQUEST_FROM:
                sendRequestFrom(message.arg1);
                break;
            case NetworkClient.REMOVE_ROUTE:
                mapManager.removeRoute();
                break;
            case NetworkClient.REBUILD_ROUTE:
                mapManager.rebuildRoute(message.arg1);
                break;
        }
        return true;
    }

    public void endRequest() {
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
        mapManager.closeMarkerWindowById();
    }

    public void setReject() {
        networkClient.setReject();
        mapManager.closeMarkerWindowById();
    }

    @Override
    public List<Integer> getPeopleIdentifiers() {
        return mapManager.getPeopleIdentifiers();
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
    public void deleteMarkers(List<Integer> markersIdentifiers) {
        mapManager.deleteMarkers(markersIdentifiers);
    }

    @Override
    public Location getLocation() {
        return mapManager.getLocation();
    }

    private void sendCompanionRequest(int id) {
        Integer companionId = id;
        networkClient.setBeginRequestTo(true, companionId);
        mapManager.blockEverything();
        mainActivityHandler.sendEmptyMessage(IS_REQUEST_TO);
    }

    private void sendRequestFrom(int id) {
        mapManager.openMarkerWindowById(id);
        mainActivityHandler.sendEmptyMessage(NetworkClient.IS_REQUEST_FROM);
    }

    private void variablesInitialization(Context context,
                                         LocationManager locationManager, MapFragment map,
                                         LayoutInflater layoutInflater, Handler mainActivityHandler) {
        this.mainActivityHandler = mainActivityHandler;
        this.locationManager = locationManager;
        this.handler = new Handler(this);
        networkClient = new NetworkClient(context, this, handler);
        routeClient = new RouteClient(handler);
        mapManager = new MapManager(context, layoutInflater, handler, routeClient);
        mapManager.uploadMap(map);
        providersService = new ProvidersService();
        gpsManager = new GPSManager(context, this, locationManager);
    }
}