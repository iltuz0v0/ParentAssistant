package net.nel.il.parentassistant.interfaces;

import android.location.Location;

import net.nel.il.parentassistant.model.InfoAccount;

import java.util.Set;

public interface MarkerStateListener {

    void refreshMarker(InfoAccount infoAccount);

    void addMarker(InfoAccount infoAccount);

    void deleteMarkers(Set<Integer> markersIdentifiers);

    Location getLocation();
}
