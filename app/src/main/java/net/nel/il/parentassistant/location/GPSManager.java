package net.nel.il.parentassistant.location;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;

import net.nel.il.parentassistant.R;
import net.nel.il.parentassistant.interfaces.ConnectionStateListener;
import net.nel.il.parentassistant.interfaces.LocationReceiver;

public class GPSManager implements LocationReceiver {

    private int minTime;

    private int minDistance;

    private LocationReceiver locationReceiver;

    private LocationManager locationManager;

    private LocationListener locationListener;

    public static final int NO_LOCATION_PERMISSION = 16;

    public GPSManager(Context context, LocationReceiver locationReceiver, LocationManager locationManager) {
        variablesInitialization(context);
        this.locationManager = locationManager;
        this.locationReceiver = locationReceiver;
    }

    @SuppressLint("MissingPermission")
    public Location findLocation(Context context, ConnectionStateListener connectionStateListener) {
        Location location = null;
        if (locationListener == null) {
            locationListener = new LocationHandler(context, this);
            if (noPermissions(context)) {
                connectionStateListener.redirectOperation(NO_LOCATION_PERMISSION);
                return null;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime * 2, minDistance, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, minDistance, locationListener);
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location == null) {
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
        }
        return location;
    }

    @Override
    public void sendLocation(Location currentLocation) {
        locationReceiver.sendLocation(currentLocation);
    }

    private void variablesInitialization(Context context) {
        minTime = context.getResources().getInteger(R.integer.min_time);
        minDistance = context.getResources().getInteger(R.integer.min_distance);
    }

    private boolean noPermissions(Context context) {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED;
    }
}
