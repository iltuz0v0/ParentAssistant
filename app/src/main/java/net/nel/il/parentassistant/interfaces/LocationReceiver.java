package net.nel.il.parentassistant.interfaces;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

public interface LocationReceiver {

    void sendLocation(Location currentLocation);
}
