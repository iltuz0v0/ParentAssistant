package net.nel.il.parentassistant.interfaces;

import android.location.Location;

import net.nel.il.parentassistant.model.InfoAccount;

import java.util.List;

public interface MarkerStateListener {

    List<Integer> getPeopleIdentifiers();

    void refreshMarker(InfoAccount infoAccount);

    void addMarker(InfoAccount infoAccount);

    void deleteMarkers(List<Integer> markersIdentifiers);

    Location getLocation();
}
