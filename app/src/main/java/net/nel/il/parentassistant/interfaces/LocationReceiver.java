package net.nel.il.parentassistant.interfaces;

import android.location.Location;

public interface LocationReceiver {

    void sendLocation(Location currentLocation);
}
