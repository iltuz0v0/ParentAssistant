package net.nel.il.parentassistant.schedule;


import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import net.nel.il.parentassistant.R;

import java.util.ArrayList;
import java.util.List;

public class SubMapFragment extends Fragment implements OnMapReadyCallback, View.OnClickListener, GoogleMap.OnMapClickListener, GoogleMap.OnMarkerDragListener {

    private static final float Z_INDEX = 100.0f;

    private GoogleMap googleMap;

    private FloatingActionButton location;

    private MapLocationCallback mapLocationCallback = null;

    private MapFragment map;

    private static int ZOOM = 15;

    private Polyline polyline;

    private LinearLayout acceptionContainer;

    private Button acception;

    private Button rejection;

    private static final int LINE_WIDTH = 10;

    private LatLng pointOne = null;

    private LatLng pointTwo = null;

    private LatLng firstClick = null;

    private LatLng secondClick = null;

    private Marker marker = null;

    @Override
    public void onMarkerDragStart(Marker marker) {
        acceptionContainer.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onMarkerDrag(Marker marker) {
        if (polyline != null) {
            polyline.remove();
        }
        drawRectangle(firstClick, marker.getPosition());
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        secondClick = marker.getPosition();
        acceptionContainer.setVisibility(View.VISIBLE);
    }

    public interface MapLocationCallback {
        Location getLocation();

        void setLocation(List<LatLng> points);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.locat:
                if (mapLocationCallback != null) {
                    moveCameraToPosition(mapLocationCallback.getLocation(), ZOOM);
                }
                break;
            case R.id.acception:
                acception();
                break;
            case R.id.rejection:
                rejection();
                break;
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        acceptionContainer.setVisibility(View.INVISIBLE);
        if (pointOne == null || pointTwo == null) {
            if (marker != null) {
                marker.remove();
            }
            if (polyline != null) {
                polyline.remove();
            }
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.draggable(true);
            changeMarkerColor(markerOptions, R.drawable.arrow);
            marker = googleMap.addMarker(markerOptions);
            firstClick = latLng;
        }
    }


    public SubMapFragment() {

    }

    private void drawRectangle(LatLng firstClick, LatLng secondClick) {
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.add(firstClick, new LatLng(firstClick.latitude, secondClick.longitude), secondClick, new LatLng(secondClick.latitude, firstClick.longitude), firstClick);
        polylineOptions.width(LINE_WIDTH);
        polylineOptions.color(getActivity().getResources().getColor(R.color.route_between));
        polyline = googleMap.addPolyline(polylineOptions);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sub_map, container, false);
        if (pointOne == null && pointTwo == null) {
            location = (FloatingActionButton) view.findViewById(R.id.locat);
            location.setOnClickListener(this);
            view.setZ(Z_INDEX);
            acceptionContainer = (LinearLayout) view.findViewById(R.id.acception_container);
            acception = (Button) view.findViewById(R.id.acception);
            acception.setOnClickListener(this);
            rejection = (Button) view.findViewById(R.id.rejection);
            rejection.setOnClickListener(this);
        }
        map = (MapFragment) getChildFragmentManager().findFragmentById(R.id.mapFr);
        map.getMapAsync(this);
        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.setOnMarkerDragListener(this);
        googleMap.setOnMapClickListener(this);
        if (pointOne == null || pointTwo == null) {
            if (mapLocationCallback != null) {
                Location location = mapLocationCallback.getLocation();
                moveCameraToPosition(location, ZOOM);
            }
        } else {
            moveCameraToPosition(getCenterLatLng(pointOne, pointTwo), ZOOM);
            drawRectangle(pointOne, pointTwo);
        }
    }

    private LatLng getCenterLatLng(LatLng pointOne, LatLng pointTwo) {
        return new LatLng((pointOne.latitude + pointTwo.latitude) / 2, (pointOne.longitude + pointTwo.longitude) / 2);
    }

    public void setReference(ScheduleFragment scheduleFragment) {
        mapLocationCallback = (MapLocationCallback) scheduleFragment;
    }

    private void moveCameraToPosition(Location currentLocation, int zoom) {
        if (googleMap != null && currentLocation != null) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude())));
            googleMap.moveCamera(CameraUpdateFactory.zoomTo(zoom));
        }
    }

    private void moveCameraToPosition(LatLng currentLocation, int zoom) {
        if (googleMap != null && currentLocation != null) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(currentLocation.latitude, currentLocation.longitude)));
            googleMap.moveCamera(CameraUpdateFactory.zoomTo(zoom));
        }
    }

    private void acception() {
        if (mapLocationCallback != null) {
            List<LatLng> points = new ArrayList<>();
            points.add(firstClick);
            points.add(secondClick);
            mapLocationCallback.setLocation(points);
        }
        getFragmentManager().popBackStack();
    }

    private void rejection() {
        acceptionContainer.setVisibility(View.INVISIBLE);
        if (polyline != null) {
            polyline.remove();
        }
        if (marker != null) {
            marker.remove();
        }
    }

    public void setPoints(LatLng pointOne, LatLng pointTwo) {
        this.pointOne = pointOne;
        this.pointTwo = pointTwo;
    }

    private void changeMarkerColor(MarkerOptions markerOptions, int drawableResource) {
        Drawable iconDrawable = getActivity().getResources().getDrawable(drawableResource);
        BitmapDescriptor icon = getMarkerIconFromDrawable(iconDrawable);
        markerOptions.icon(icon);
    }

    private BitmapDescriptor getMarkerIconFromDrawable(Drawable drawable) {
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }


}
