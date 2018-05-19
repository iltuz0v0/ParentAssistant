package net.nel.il.parentassistant.main;

import android.os.Handler;
import android.os.Message;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RouteClient {

    private final String routes = "routes";

    private final String legs = "legs";

    private final String steps = "steps";

    private final String polyline = "polyline";

    private final String points = "points";

    private final String lat = "lat";

    private final String lng = "lng";

    private ExecutorService executor;

    private Handler handler;

    public static final int HANDLER_DRAW_ROUTE = 0;

    RouteClient(Handler handler) {
        this.handler = handler;
        executor = Executors.newFixedThreadPool(1);
    }

    private List<List<HashMap<String, String>>> parseJSON(String json)
            throws JSONException {
        List<List<HashMap<String, String>>> points = new ArrayList<>();
        if (json != null) {
            JSONObject jsonRoutes = new JSONObject(json);
            JSONArray routes = jsonRoutes.getJSONArray(this.routes);
            for (int route = 0; route < routes.length(); route++) {
                JSONArray legs = ((JSONObject) routes.get(route))
                        .getJSONArray(this.legs);
                for (int leg = 0; leg < legs.length(); leg++) {
                    List<HashMap<String, String>> oneLeg = new ArrayList<>();
                    JSONArray steps = ((JSONObject) legs.get(leg))
                            .getJSONArray(this.steps);
                    for (int step = 0; step < steps.length(); step++) {
                        String pointsString = ((JSONObject) steps.get(step))
                                .getJSONObject(this.polyline).getString(this.points);
                        List<LatLng> pointsList = PolyUtil.decode(pointsString);
                        for (int point = 0; point < pointsList.size(); point++) {
                            HashMap<String, String> latlngPoints = new HashMap<>();
                            latlngPoints.put(this.lat,
                                    Double.toHexString(pointsList.get(point).latitude));
                            latlngPoints.put(this.lng,
                                    Double.toHexString(pointsList.get(point).longitude));
                            oneLeg.add(latlngPoints);
                        }
                    }
                    points.add(oneLeg);
                }
            }

        } else {
            // InternetProblems
        }
        return points;
    }


    public void getJSON(final LatLng origin, final LatLng destination) {
        Callable<Integer> callable = new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                StringBuilder json = new StringBuilder();
                HttpURLConnection connection = null;
                try {
                    URL routeURL = new URL(getRouteURL(origin, destination));
                    connection = (HttpURLConnection) routeURL.openConnection();
                    connection.connect();
                } catch (IOException e) {
                    return null;
                }
                try (InputStream inputStream = connection.getInputStream()) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    String jsonLine;
                    while ((jsonLine = reader.readLine()) != null) {
                        json.append(jsonLine);
                    }
                } catch (IOException e) {
                    return null;
                } finally {
                    connection.disconnect();
                }
                drawRoute(parseJSON(json.toString()));
                return 0;
            }
        };
        executor.submit(callable);
    }

    private String getRouteURL(LatLng origin, LatLng destination) {
        return "https://maps.googleapis.com/maps/api/directions/" +
                "json" + "?" +
                "origin=" + origin.latitude + "," + origin.longitude + "&" +
                "destination=" + destination.latitude + "," + destination.longitude + "&" +
                "sensor=false" + "&" +
                "mode=walking";
    }

    private void drawRoute(List<List<HashMap<String, String>>> route) {
        Message message = new Message();
        message.obj = route;
        message.what = HANDLER_DRAW_ROUTE;
        handler.sendMessage(message);
    }
}
