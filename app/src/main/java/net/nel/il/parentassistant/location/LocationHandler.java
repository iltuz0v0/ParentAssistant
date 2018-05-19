package net.nel.il.parentassistant.location;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.gms.maps.model.LatLng;

import net.nel.il.parentassistant.R;
import net.nel.il.parentassistant.interfaces.LocationReceiver;

public class LocationHandler implements LocationListener {

    private LocationReceiver locationReceiver;

    private int amountGPSReplies = 0;

    private boolean isEnabledGPS = false;

    private int switchingSteps;

    LocationHandler(Context context, LocationReceiver locationReceiver) {
        variablesInitialization(context);
        this.locationReceiver = locationReceiver;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
            isEnabledGPS = true;
        }
        if (location.getProvider().equals(LocationManager.NETWORK_PROVIDER)
                && amountGPSReplies < switchingSteps) {
            locationReceiver.sendLocation(getLatLngFromLocation(location));
            amountGPSReplies++;
        } else if (location.getProvider().equals(LocationManager.NETWORK_PROVIDER)
                && amountGPSReplies >= switchingSteps && !isEnabledGPS) {
            locationReceiver.sendLocation(getLatLngFromLocation(location));
        } else if (location.getProvider().equals(LocationManager.GPS_PROVIDER)
                && amountGPSReplies == 0) {
            amountGPSReplies = switchingSteps;
        }
        if (location.getProvider().equals(LocationManager.GPS_PROVIDER)
                && amountGPSReplies >= switchingSteps) {
            locationReceiver.sendLocation(getLatLngFromLocation(location));
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    private LatLng getLatLngFromLocation(Location location) {
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

    private void variablesInitialization(Context context) {
        switchingSteps = context.getResources().getInteger(R.integer.switching_steps);
    }
}
