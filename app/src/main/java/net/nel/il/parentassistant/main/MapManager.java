package net.nel.il.parentassistant.main;


import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.os.Handler;
import android.view.LayoutInflater;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import net.nel.il.parentassistant.R;
import net.nel.il.parentassistant.model.InfoAccount;
import net.nel.il.parentassistant.settings.SharedPreferenceManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MapManager implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener {

    private GoogleMap googleMap = null;

    private int lastLocationZoom;

    private int locationZoom;

    private Location lastLocation;

    private Circle client = null;

    private Circle circle = null;

    public static final double courseMeter = 0.000009;

    public static float defaultCircleRadius = 100;

    private double clientCircleRadius;

    private float widthLine;

    private float widthCircle;

    private float widthClientCircle;

    private final String latitude = "lat";

    private final String longitude = "lng";

    private Map<Integer, Marker> markers;

    private Map<Integer, List<InfoAccount>> infoAccounts;

    private SharedPreferenceManager sharedPreferenceManager;

    private InfoWindow infoWindow;

    private List<Integer> identifiers;

    private boolean isBlock = false;

    public static final int COMPANION_REQUEST = 4;

    private int openedMarker = -1;

    private Polyline route = null;

    private RouteClient routeClient;

    private boolean isCourseCameraMove = false;

    private boolean isFineCameraMove = false;

    public MapManager(Context context,
                      LayoutInflater layoutInflater, Handler handler,
                      RouteClient routeClient) {
        variablesInitialization(context, layoutInflater, handler, routeClient);
    }

    public void uploadMap(MapFragment mapFragment) {
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.setOnMarkerClickListener(this);
        createStartPosition();

    }

    public void setAdjustedLocation(Location lastLocation, Context context) {
        float radius = sharedPreferenceManager.getValue(context, R.string.settings_file,
                R.string.setting_radius);
        if (radius != -1.0f) {
            defaultCircleRadius = radius;
        }
        if(!isCourseCameraMove){
            createStartPosition(new LatLng(lastLocation.getLatitude(),
                    lastLocation.getLongitude()));
        }
        this.lastLocation = lastLocation;
    }

    private void createStartPosition() {
        if (lastLocation != null) {
            isCourseCameraMove = true;
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(
                    lastLocation.getLatitude(), lastLocation.getLongitude())));
            googleMap.moveCamera(CameraUpdateFactory.zoomTo(lastLocationZoom));
        }
    }

    private void createStartPosition(LatLng currentLocation) {
        if(googleMap != null && currentLocation != null) {
            isCourseCameraMove = true;
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(
                    currentLocation.latitude, currentLocation.longitude)));
            googleMap.moveCamera(CameraUpdateFactory.zoomTo(locationZoom));
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
                points.add(new LatLng(Double.parseDouble(latitude),
                        Double.parseDouble(longitude)));
            }
            lineOptions.addAll(points);
        }
        lineOptions.color(Color.rgb(80, 110, 50));
        lineOptions.width(widthLine);
        if (route != null) {
            route.remove();
        }
        route = googleMap.addPolyline(lineOptions);
    }

    public void adjustLocation(LatLng currentLocation) {
        if (googleMap != null) {
            if (lastLocation == null || !isFineCameraMove) {
                createStartPosition(currentLocation);
                isFineCameraMove = true;
            }
            if (client == null) {
                CircleOptions clientOptions = getCircle(currentLocation,
                        clientCircleRadius, Color.argb(100, 220, 40, 40),
                        widthClientCircle, Color.rgb(190, 30, 30));
                client = googleMap.addCircle(clientOptions);
                CircleOptions circleOptions = getCircle(currentLocation,
                        defaultCircleRadius, Color.argb(25, 90, 140, 90), widthCircle, Color.rgb(80, 150, 70));
                circle = googleMap.addCircle(circleOptions);
                googleMap.moveCamera(CameraUpdateFactory.zoomTo(locationZoom));
            } else {
                client.setCenter(currentLocation);
                circle.setCenter(currentLocation);
            }
        }
    }

    public void changeRadius(Context context) {
        float radius = sharedPreferenceManager.getValue(context,
                R.string.settings_file, R.string.setting_radius);
        if (radius != -1.0) {
            if (circle != null) {
                circle.setRadius((double) radius);
                defaultCircleRadius = radius;
            }
        }
    }

    private CircleOptions getCircle(LatLng center, double radius, int fillColor,
                                    float strokeWidth, int strokeColor) {
        return new CircleOptions()
                .center(center)
                .radius(radius)
                .fillColor(fillColor)
                .strokeWidth(strokeWidth)
                .strokeColor(strokeColor);
    }

    public List<Integer> getPeopleIdentifiers() {
        return identifiers;
    }

    public void refreshMarker(InfoAccount infoAccount) {
        markers.get(infoAccount.getIdentifier())
                .setPosition(new LatLng(infoAccount.getLatitude(),
                infoAccount.getLongitude()));
        infoAccounts.get(infoAccount.getIdentifier())
                .get(0).setStatus(infoAccount.getStatus());
    }

    public void addMarker(InfoAccount infoAccount) {
        if (markers.get(infoAccount.getIdentifier()) == null) {
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(new LatLng(infoAccount.getLatitude(),
                    infoAccount.getLongitude()));
            Marker marker = googleMap.addMarker(markerOptions);
            marker.setTag(infoAccount.getIdentifier());
            markers.put(infoAccount.getIdentifier(), marker);
            List<InfoAccount> listInfoAccounts = new LinkedList<>();
            listInfoAccounts.add(infoAccount);
            infoAccounts.put(infoAccount.getIdentifier(), listInfoAccounts);
        } else {
            boolean result = true;
            for (InfoAccount infoAccountElement :
                    infoAccounts.get(infoAccount.getIdentifier())) {
                if (infoAccount.equals(infoAccountElement)) {
                    result = false;
                }
            }
            if (result) {
                infoAccounts.get(infoAccount.getIdentifier()).add(infoAccount);
            }
        }
        identifiers = new LinkedList<>();
        for (Marker marker : markers.values()) {
            identifiers.add((Integer) marker.getTag());
        }
    }

    public void deleteMarkers(List<Integer> markersIdentifiers) {
        for (int element : markersIdentifiers) {
            markers.get(element).remove();
            markers.remove(element);
            infoAccounts.remove(element);
        }
        identifiers = new LinkedList<>();
        for (Marker marker : markers.values()) {
            identifiers.add((Integer) marker.getTag());
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

    public boolean isBlock() {
        return isBlock;
    }

    public void openMarkerWindowById(Integer companionId) {
        markers.get(companionId).showInfoWindow();
        openedMarker = companionId;
    }

    public void closeMarkerWindowById() {
        markers.get(openedMarker).hideInfoWindow();
        openedMarker = -1;
    }

    public void removeRoute() {
        if (route != null) {
            route.remove();
        }
    }

    public void rebuildRoute(int companionId) {
        routeClient.getJSON(new LatLng(lastLocation.getLatitude(),
            lastLocation.getLongitude()),
            markers.get(companionId).getPosition());
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        infoWindow.createCompanionRequestWindow(
            getInfoAccounts().get((Integer) marker.getTag()),
            (Integer) marker.getTag());
        return true;
    }

    private void variablesInitialization(final Context context,
            LayoutInflater layoutInflater, Handler handler, RouteClient routeClient) {
        this.routeClient = routeClient;
        identifiers = new LinkedList<>();
        infoWindow = new InfoWindow(layoutInflater,
                context, handler);
        markers = new TreeMap<>();
        infoAccounts = new TreeMap<>();
        sharedPreferenceManager = new SharedPreferenceManager();
        lastLocationZoom = context.getResources()
                .getInteger(R.integer.last_location_zoom);
        locationZoom = context.getResources()
                .getInteger(R.integer.location_zoom);
        defaultCircleRadius = context.getResources()
                .getInteger(R.integer.default_circle_radius);
        clientCircleRadius = context.getResources()
                .getInteger(R.integer.client_circle_radius);
        widthLine = context.getResources()
                .getInteger(R.integer.width_line);
        widthCircle = context.getResources()
                .getInteger(R.integer.width_circle);
        widthClientCircle = context.getResources()
                .getInteger(R.integer.width_client_circle);
    }

}

