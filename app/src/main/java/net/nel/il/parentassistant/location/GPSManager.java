package net.nel.il.parentassistant.location;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import com.google.android.gms.maps.model.LatLng;

import net.nel.il.parentassistant.R;
import net.nel.il.parentassistant.interfaces.LocationReceiver;

public class GPSManager implements LocationReceiver {

    private int minTime;

    private int minDistance;

    private LocationReceiver locationReceiver;

    private LocationManager locationManager;

    private LocationListener locationListener;

    public GPSManager(Context context, LocationReceiver locationReceiver,
                      LocationManager locationManager) {
        variablesInitialization(context);
        this.locationManager = locationManager;
        this.locationReceiver = locationReceiver;
    }

    public Location findLocation(Context context) {
        Location location = null;
        if (locationListener == null) {
            locationListener = new LocationHandler(context, this);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    minTime, minDistance, locationListener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    minTime * 2, minDistance, locationListener);
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location == null) {
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
        }
        return location;
    }

    @Override
    public void sendLocation(LatLng currentLocation) {
        locationReceiver.sendLocation(currentLocation);
    }

    private void variablesInitialization(Context context) {
        minTime = context.getResources().getInteger(R.integer.min_time);
        minDistance = context.getResources().getInteger(R.integer.min_distance);
    }
}
