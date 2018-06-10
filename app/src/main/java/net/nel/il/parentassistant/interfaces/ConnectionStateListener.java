package net.nel.il.parentassistant.interfaces;

import net.nel.il.parentassistant.model.InfoAccount;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

public interface ConnectionStateListener {

    void redirectOperation(int code);

    void redirectRemovingRoute(int code);

    void redirectRebuildingRoute(int code, int companionId);

    void redirectIsRequestFrom(int code, int companionId, int type);

    void redirectRefreshingChat(int code);

    void redirectClearingMessages(int code);

    void redirectMarkerRefreshing(int code, InfoAccount infoAccount);

    void redirectMarkerAddition(int code, InfoAccount infoAccount);

    void redirectMarkerDeleting(int code, Set<Integer> markers);

    void redirectRouteDrawing(int code, List<List<HashMap<String, String>>> route);

}
