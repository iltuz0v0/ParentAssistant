package net.nel.il.parentassistant.main;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Handler;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import net.nel.il.parentassistant.R;
import net.nel.il.parentassistant.interfaces.ConnectionStateListener;
import net.nel.il.parentassistant.model.InfoAccount;
import net.nel.il.parentassistant.network.NetworkClient;
import net.nel.il.parentassistant.settings.SharedPreferenceManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class MapManager implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap googleMap = null;

    private int lastLocationZoom;

    private int locationZoom;

    private Location lastLocation;

    private volatile Circle client = null;

    private volatile Circle circle = null;

    public static final double courseMeter = 0.000009;

    public static float defaultCircleRadius;

    private double clientCircleRadius;

    private float widthLine;

    private float widthCircle;

    private float widthClientCircle;

    private final String latitude = "lat";

    private final String longitude = "lng";

    private Map<Integer, Marker> markers;

    private Map<Integer, List<InfoAccount>> infoAccounts;

    private SharedPreferenceManager sharedPreferenceManager;

    public static final int IS_DESTINATION_MARKER = 1;

    public static final int NOT_DESTINATION_MARKER = -1;

    private boolean isBlock = false;

    public static final int COMPANION_REQUEST = 4;

    private volatile Polyline route = null;

    private RouteClient routeClient;

    private boolean isCourseCameraMove = false;

    private boolean isFineCameraMove = false;

    private boolean isRequestColor = false;

    private boolean isCreated = false;

    private PolylineOptions lastRoute = null;

    private Handler mainActivityHandler;

    private ConnectionStateListener connectionStateListener;

    private Context context;

    private int specialMarkerId = -1;

    public MapManager(Context context, ConnectionStateListener connectionStateListener, Handler mainActivityHandler, RouteClient routeClient) {
        this.context = context;
        this.mainActivityHandler = mainActivityHandler;
        variablesInitialization(context, connectionStateListener, routeClient);
    }

    public void uploadMap(MapFragment mapFragment) {
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        if (isCreated) {
            refreshMap();
        } else {
            createStartPosition();
        }
        googleMap.setOnMarkerClickListener(this);
        isCreated = true;

    }

    public void setHandler(Handler mainHandler) {
        mainActivityHandler = mainHandler;
    }

    public void setAdjustedLocation(Location lastLocation, Context context) {
        float radius = sharedPreferenceManager.getValue(context, R.string.settings_file, R.string.setting_radius);
        if (radius != -1.0f) {
            defaultCircleRadius = radius;
        }
        if (!isCourseCameraMove && lastLocation != null) {
            createStartPosition(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()));
        }
        this.lastLocation = lastLocation;
    }

    private void createStartPosition() {
        moveCameraToLastPosition(lastLocationZoom);
    }

    private void createStartPosition(LatLng currentLocation) {
        moveCameraToPosition(currentLocation, locationZoom);
    }

    public void findCurrentLocation() {
        moveCameraToLastPosition(locationZoom);
    }

    private void moveCameraToLastPosition(int zoom) {
        if (googleMap != null && lastLocation != null) {
            isCourseCameraMove = true;
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude())));
            googleMap.moveCamera(CameraUpdateFactory.zoomTo(zoom));
        }
    }

    private void moveCameraToPosition(LatLng currentLocation, int zoom) {
        if (googleMap != null && currentLocation != null) {
            isCourseCameraMove = true;
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(currentLocation.latitude, currentLocation.longitude)));
            googleMap.moveCamera(CameraUpdateFactory.zoomTo(zoom));
        }
    }


    public void drawRoute(List<List<HashMap<String, String>>> routes) {
        PolylineOptions lineOptions = new PolylineOptions();
        for (int route = 0; route < routes.size(); route++) {
            List<HashMap<String, String>> legs = routes.get(route);
            ArrayList<LatLng> points = new ArrayList<>();
            lineOptions = new PolylineOptions();
            for (int leg = 0; leg < legs.size(); leg++) {
                String latitude = legs.get(leg).get(this.latitude);
                String longitude = legs.get(leg).get(this.longitude);
                points.add(new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude)));
            }
            lineOptions.addAll(points);
        }
        if (isRequestColor) {
            lineOptions.color(context.getResources().getColor(R.color.route_available));
            isRequestColor = false;
        } else {
            lineOptions.color(context.getResources().getColor(R.color.route_between));
        }
        lineOptions.width(widthLine);
        if (lineOptions.getPoints().size() != 0) {
            removeRoute();
            route = googleMap.addPolyline(lineOptions);
            lastRoute = lineOptions;
        }
    }

    public void adjustLocation(Location currentLocation) {
        this.lastLocation = currentLocation;
        if (googleMap != null && currentLocation != null) {
            LatLng crntLocation = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            if (lastLocation == null || !isFineCameraMove) {
                createStartPosition(crntLocation);
                isFineCameraMove = true;
            }
            if (client == null) {
                CircleOptions clientOptions = getClientCircle(crntLocation);
                client = googleMap.addCircle(clientOptions);
                CircleOptions circleOptions = getOuterCircle(crntLocation);
                circle = googleMap.addCircle(circleOptions);
                googleMap.moveCamera(CameraUpdateFactory.zoomTo(locationZoom));
            } else {
                client.setCenter(crntLocation);
                circle.setCenter(crntLocation);
            }
        }
    }

    public void changeRadius(Context context) {
        float radius = sharedPreferenceManager.getValue(context, R.string.settings_file, R.string.setting_radius);
        if (radius != -1.0) {
            if (circle != null) {
                circle.setRadius((double) radius);
                defaultCircleRadius = radius;
            }
        }
    }

    private CircleOptions getClientCircle(LatLng center) {
        return new CircleOptions().center(center).radius(clientCircleRadius).fillColor(context.getResources().getColor(R.color.client_circle_fill)).strokeWidth(widthClientCircle).strokeColor(context.getResources().getColor(R.color.client_circle_stroke));
    }

    private CircleOptions getOuterCircle(LatLng center) {
        return new CircleOptions().center(center).radius(defaultCircleRadius).fillColor(context.getResources().getColor(R.color.outer_circle_fill)).strokeWidth(widthCircle).strokeColor(context.getResources().getColor(R.color.outer_circle_stroke));
    }

    public void refreshMarker(InfoAccount infoAccount) {
        markers.get(infoAccount.getIdentifier()).setPosition(new LatLng(infoAccount.getLatitude(), infoAccount.getLongitude()));
        infoAccounts.get(infoAccount.getIdentifier()).get(0).setStatus(infoAccount.getStatus());
    }

    public void addMarker(InfoAccount infoAccount) {
        if (markers.get(infoAccount.getIdentifier()) == null) {
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(new LatLng(infoAccount.getLatitude(), infoAccount.getLongitude()));
            Marker marker = googleMap.addMarker(markerOptions);
            if (infoAccount.getIdentifier() == specialMarkerId) {
                changeMarkerColor(marker, R.drawable.marker);
            } else {
                changeMarkerColor(marker, R.drawable.changed_marker);
            }
            marker.setTag(infoAccount.getIdentifier());
            markers.put(infoAccount.getIdentifier(), marker);
            List<InfoAccount> listInfoAccounts = new LinkedList<>();
            listInfoAccounts.add(infoAccount);
            infoAccounts.put(infoAccount.getIdentifier(), listInfoAccounts);
        } else {
            boolean result = true;
            for (InfoAccount infoAccountElement : infoAccounts.get(infoAccount.getIdentifier())) {
                if (infoAccount.equals(infoAccountElement)) {
                    result = false;
                }
            }
            if (result) {
                infoAccounts.get(infoAccount.getIdentifier()).add(infoAccount);
            }
        }
    }

    public void deleteMarkers(Set<Integer> markersIdentifiers) {
        for (int element : markersIdentifiers) {
            markers.get(element).remove();
            markers.remove(element);
            infoAccounts.remove(element);
        }
    }

    private Map<Integer, List<InfoAccount>> getInfoAccounts() {
        return infoAccounts;
    }

    public Location getLocation() {
        return lastLocation;
    }

    public void blockEverything() {
        isBlock = true;
    }

    public void unblockEverything() {
        isBlock = false;
    }

    public int illuminateMarker(int companionId) {
        int result;
        if (markers.get(companionId) != null) {
            isRequestColor = true;
            rebuildRoute(companionId);
            changeMarkerColorToSpecial(companionId);
            result = IS_DESTINATION_MARKER;
        } else {
            result = NOT_DESTINATION_MARKER;
        }
        return result;
    }

    public void buildPossibleRoute(int companionId) {
        isRequestColor = true;
        rebuildRoute(companionId);
    }

    public void changeMarkerColorToDefault(int companionId) {
        Marker marker;
        if ((marker = markers.get(companionId)) != null) {
            specialMarkerId = -1;
            changeMarkerColor(marker, R.drawable.changed_marker);
        }
    }

    public void changeMarkerColorToSpecial(int companionId) {
        Marker marker;
        if ((marker = markers.get(companionId)) != null) {
            specialMarkerId = companionId;
            changeMarkerColor(marker, R.drawable.marker);
        }
    }

    private void changeMarkerColor(Marker marker, int drawableResource) {
        Drawable iconDrawable = context.getResources().getDrawable(drawableResource);
        BitmapDescriptor icon = getMarkerIconFromDrawable(iconDrawable);
        marker.setIcon(icon);
    }

    private BitmapDescriptor getMarkerIconFromDrawable(Drawable drawable) {
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    public void removeRoute() {
        if (route != null) {
            route.remove();
            lastRoute = null;
        }
    }

    public void rebuildRoute(int companionId) {
        if (markers.get(companionId) != null) {
            System.out.println("CURRENT " + System.currentTimeMillis());
            routeClient.getJSON(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()), markers.get(companionId).getPosition());
        } else {
            if (NetworkClient.companionId > 0) {
                connectionStateListener.redirectOperation(NetworkClient.COMPANION_IS_OFFLINE);
            }
        }
    }

    @Override
    @SuppressWarnings("all")
    public boolean onMarkerClick(Marker marker) {
        if (!isBlock || NetworkClient.companionId == (Integer) marker.getTag()) {
            int isCompanion = 0;
            if (NetworkClient.companionId == (Integer) marker.getTag()) {
                isCompanion = 1;
            }
            android.os.Message message = mainActivityHandler.obtainMessage();
            message.what = InfoWindow.SHOW_INFO_WINDOW;
            message.obj = getInfoAccounts().get((Integer) marker.getTag());
            message.arg1 = (Integer) marker.getTag();
            message.arg2 = isCompanion;
            mainActivityHandler.sendMessage(message);
        }
        return true;
    }

    private void refreshMap() {
        if (lastLocation != null) {
            LatLng currentLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
            client = googleMap.addCircle(getClientCircle(currentLocation));
            circle = googleMap.addCircle(getOuterCircle(currentLocation));
            createStartPosition(currentLocation);
        }
        if (lastRoute != null) {
            route = googleMap.addPolyline(lastRoute);
        }
        Map<Integer, List<InfoAccount>> infoAccountsCopy = new TreeMap<>(infoAccounts);
        infoAccounts.clear();
        markers.clear();
        for (List<InfoAccount> infoAccount : infoAccountsCopy.values()) {
            for (InfoAccount infoAccountPart : infoAccount) {
                addMarker(infoAccountPart);
            }
        }
    }

    private void variablesInitialization(final Context context, ConnectionStateListener connectionStateListener, RouteClient routeClient) {
        this.routeClient = routeClient;
        this.connectionStateListener = connectionStateListener;
        markers = new TreeMap<>();
        infoAccounts = new TreeMap<>();
        sharedPreferenceManager = new SharedPreferenceManager();
        lastLocationZoom = context.getResources().getInteger(R.integer.last_location_zoom);
        locationZoom = context.getResources().getInteger(R.integer.location_zoom);
        defaultCircleRadius = context.getResources().getInteger(R.integer.default_circle_radius);
        clientCircleRadius = context.getResources().getInteger(R.integer.client_circle_radius);
        widthLine = context.getResources().getInteger(R.integer.width_line);
        widthCircle = context.getResources().getInteger(R.integer.width_circle);
        widthClientCircle = context.getResources().getInteger(R.integer.width_client_circle);
    }

}
