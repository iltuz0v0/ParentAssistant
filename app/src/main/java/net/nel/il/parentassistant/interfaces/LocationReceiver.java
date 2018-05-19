package net.nel.il.parentassistant.interfaces;

import com.google.android.gms.maps.model.LatLng;

public interface LocationReceiver {

    void sendLocation(LatLng currentLocation);
}
