package net.nel.il.parentassistant.network;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

import net.nel.il.parentassistant.FileManager;
import net.nel.il.parentassistant.Messaging.Messaging;
import net.nel.il.parentassistant.R;
import net.nel.il.parentassistant.account.AccountDataHandler;
import net.nel.il.parentassistant.interfaces.ConnectionStateListener;
import net.nel.il.parentassistant.interfaces.MarkerStateListener;
import net.nel.il.parentassistant.main.MainActivity;
import net.nel.il.parentassistant.main.MapManager;
import net.nel.il.parentassistant.model.InfoAccount;
import net.nel.il.parentassistant.model.InputAccount;
import net.nel.il.parentassistant.model.OutputAccount;
import net.nel.il.parentassistant.settings.SharedPreferenceManager;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class NetworkClientMessaging {

    private AccountDataHandler accountDataHandler;

    private MarkerStateListener markerReceiver;

    private Context context;

    public static final int MARKER_REFRESHING = 1;

    public static final int MARKER_ADDITION = 2;

    public static final int MARKER_DELETING = 3;

    private static final int NO_IDENTIFIER = -1;

    static final int SENDING_MESSAGE = 13;

    static final int CHAT_REFRESHING = 14;

    private Set<Integer> identifiers;

    private Messaging messaging;

    private boolean isInternet = true;

    private static int FIRST_CLICK = 0;

    private static int SECOND_CLICK = 1;

    private static final int QUALITY_COMPRESS = 100;

    private ConnectionStateListener connectionStateListener;

    private static String DEFAULT_ADDRESS = "https://mthrsassistant.herokuapp.com/";

    private static String SPECIAL_ADDRESS = "https://mthrsassistant.herokuapp.com/special/";

    public static final String DEFAULT_OUTPUT_JSON = "{\"id\":-1}";

    NetworkClientMessaging(Context context, ConnectionStateListener connectionStateListener,
                                  MarkerStateListener markerReceiver,
                                  Messaging messaging) {
        this.connectionStateListener = connectionStateListener;
        this.messaging = messaging;
        accountDataHandler = new AccountDataHandler(
                new FileManager(context.getResources()
                        .getString(R.string.user_data_file)));
        this.markerReceiver = markerReceiver;
        this.context = context;
        identifiers = new TreeSet<>();
    }

    public OutputAccount sendDataRequest() {
        Location currentLocation = markerReceiver.getLocation();
        OutputAccount account;
        if(currentLocation != null) {
            account = sendDataRequest(currentLocation);
        }else {
            account = new OutputAccount();
            account.setId(NetworkClient.UPDATE);
        }
        return account;
    }

    private OutputAccount sendDataRequest(Location currentLocation){
        OutputAccount account;
        int identifier = SharedPreferenceManager.getIdentifier(context);
        boolean isDataUpdated = SharedPreferenceManager.isUpdated(context);
        if (identifier == NO_IDENTIFIER) {
            account = sendDataRequest(NetworkClient.ACCOUNT_CREATING,
                    identifier, currentLocation);
        } else {
            if (isDataUpdated) {
                account = sendDataRequest(NetworkClient.UPDATE,
                        identifier, currentLocation);
            } else {
                account = sendDataRequest(NetworkClient.GETTING_DATA, identifier,
                        currentLocation);
            }
        }
        return account;
    }

    private OutputAccount sendDataRequest(int id,
                                          int identifier, Location currentLocation) {
        String json = translateToJSON(id, identifier, currentLocation);
        System.out.println("input json " + json);
        String outputJson = sendHttpRequest(json, DEFAULT_ADDRESS);
        System.out.println("output json" + outputJson);
        OutputAccount account = translateFromJson(outputJson);
        getMessages(account);
        if(account.getId() == NetworkClient.ACCOUNT_CREATING){
            SharedPreferenceManager.saveIdentifier(account.getIdentifier(), context);
        }
        if(account.getId() == NetworkClient.GETTING_DATA){
            outputServerData(account);
        }
        return account;
    }

    private String translateToJSON(int id,
                                   int identifier, Location currentLocation) {
        Gson gson = new Gson();
        InputAccount account = getDefaultInputAccount(id, identifier,
                currentLocation, NetworkClient.companionId);
        if (id == NetworkClient.ACCOUNT_CREATING || id == NetworkClient.UPDATE) {
            getAccountData(account);
        }
        if(id == NetworkClient.ACCOUNT_CREATING){
            SharedPreferenceManager.getGoogleIdentifier(account, context);
        }
        if (id == NetworkClient.GETTING_DATA) {
            addMessagesAmount(account);
            getSentPeopleIdentifiers(account);
        }
        return gson.toJson(account);
    }

    private InputAccount getDefaultInputAccount(int id, int identifier,
                                                Location currentLocation,
                                                int companionId){
        return new InputAccount(id, identifier,
                (float) (MapManager.defaultCircleRadius * MapManager.courseMeter),
                currentLocation.getLatitude(),
                currentLocation.getLongitude(), companionId);
    }

    private InputAccount getDefaultInputAccount(int id, int identifier,
                                                Location currentLocation){
        return new InputAccount(id, identifier,
                (float) (MapManager.defaultCircleRadius * MapManager.courseMeter),
                currentLocation.getLatitude(),
                currentLocation.getLongitude());
    }

    private void addMessagesAmount(InputAccount inputAccount){
        inputAccount.setMessagesAmount(messaging.getOuterMessagesAmount());
    }

    private void getMessages(OutputAccount outputAccount){
        if(outputAccount != null && outputAccount.getMessages() != null &&
                outputAccount.getMessages().size() > 0) {
            messaging.addOuterMessages(outputAccount);
            connectionStateListener.redirectRefreshingChat(CHAT_REFRESHING);
        }
    }

    private OutputAccount translateFromJson(String outputJson) {
        Gson gson = new Gson();
        return gson.fromJson(outputJson, OutputAccount.class);
    }

    private void outputServerData(OutputAccount account) {
        List<Integer> peopleIdentifiers = account.getPeopleIdentifiers();
        Set<Integer> list = new TreeSet<>(identifiers);
        for (int element = 0; element < peopleIdentifiers.size(); element++) {
            if (list.remove(peopleIdentifiers.get(element))) {
                refreshMarker(element, account, peopleIdentifiers);
            } else {
                identifiers.add(peopleIdentifiers.get(element));
                addMarker(element, account, peopleIdentifiers);
            }
        }
        if (list.size() > 0) {
            for(Integer element : list){
                identifiers.remove(element);
            }
            deleteMarker(list);
        }
    }

    private void getAccountData(InputAccount account) {
        SharedPreferenceManager.getGoogleIdentifier(account, context);
        String[] mapTags = context.getResources().getStringArray(R.array.mapTags);
        ArrayList<HashMap<String, Object>> data = accountDataHandler.handleData(context,
                mapTags);
        handleAccountData(data, mapTags, account);
    }

    private void handleAccountData(ArrayList<HashMap<String, Object>> data,
                                   String[] mapTags, InputAccount account) {
        ArrayList<String> names = new ArrayList<>();
        ArrayList<String> ages = new ArrayList<>();
        ArrayList<String> hobbies = new ArrayList<>();
        ArrayList<String> photos = new ArrayList<>();
        int mapTagIndex = 0;
        for (int element = 0; element < data.size(); element++) {
            HashMap<String, Object> dataElement = data.get(element);
            photos.add(encodeBitmap((Bitmap) dataElement.get(mapTags[mapTagIndex++])));
            names.add((String) dataElement.get(mapTags[mapTagIndex++]));
            ages.add((String) dataElement.get(mapTags[mapTagIndex++]));
            hobbies.add((String) dataElement.get(mapTags[mapTagIndex]));
            mapTagIndex = 0;
        }
        account.setName(names);
        account.setAge(ages);
        account.setHobbies(hobbies);
        account.setPhotos(photos);
    }

    private void getSentPeopleIdentifiers(InputAccount account) {
        account.setPeopleIdentifiers(new ArrayList<>(identifiers));
    }

    private String encodeBitmap(Bitmap bitmap) {
        byte[] byteArray = null;
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, QUALITY_COMPRESS, stream);
            byteArray = stream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private String sendHttpRequest(final String json, String address) {
        if(!hasInternetConnection(context)){
            if(isInternet) {
                isInternet = false;
                connectionStateListener.redirectOperation(NetworkClient.NO_INTERNET);
            }
            return DEFAULT_OUTPUT_JSON;
        }else{
            isInternet = true;
        }
        final StringBuilder outputJson = new StringBuilder();
        BufferedReader reader = null;
        BufferedOutputStream writer = null;
        try {
            URL url = new URL(address);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            writer = new BufferedOutputStream(connection.getOutputStream());
            writer.write(json.getBytes());
            writer.flush();
            writer.close();
            connection.connect();
            reader = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String buffer;
            while ((buffer = reader.readLine()) != null) {
                outputJson.append(buffer);
            }
            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            sendHttpRequest(json, address);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return outputJson.toString();
    }

    public void requestTo(int id, int companionId, int type) {
        String json = translateToMinJSON(id, companionId, type);
        sendHttpRequest(json, DEFAULT_ADDRESS);
        connectionStateListener.redirectClearingMessages(NetworkClient.CLEAR_MESSAGES);
    }

    public void acceptTo(int id, int companionId) {
        String json = translateToMinJSON(id, companionId);
        sendHttpRequest(json, DEFAULT_ADDRESS);
    }

    public void rejectTo(int id, int companionId) {
        String json = translateToMinJSON(id, companionId);
        sendHttpRequest(json, DEFAULT_ADDRESS);
    }

    public void doNoConnectionTo(int id, int companionId) {
        String json = translateToMinJSON(id, companionId);
        sendHttpRequest(json, DEFAULT_ADDRESS);
    }

    public void breakTo(int id, int companionId) {
        String json = translateToMinJSON(id, companionId);
        sendHttpRequest(json, DEFAULT_ADDRESS);
    }

    public String sendMessage(String message, int companionId){
        String json = translateToMessageJSON(message, companionId);
        return sendHttpRequest(json, DEFAULT_ADDRESS);
    }

    private String translateToMessageJSON(String message, int companionId) {
        Gson gson = new Gson();
        InputAccount account = new InputAccount();
        account.setId(SENDING_MESSAGE);
        account.setMessage(message);
        account.setIdentifier(SharedPreferenceManager.getIdentifier(context));
        account.setCompanion_id(companionId);
        return gson.toJson(account);
    }

    private String translateToMinJSON(int id, int companionId) {
        Gson gson = new Gson();
        InputAccount account = new InputAccount();
        account.setId(id);
        account.setIdentifier(SharedPreferenceManager.getIdentifier(context));
        account.setCompanion_id(companionId);
        return gson.toJson(account);
    }

    private String translateToMinJSON(int id, List<LatLng> points, String from,
                                      String to) {
        Gson gson = new Gson();
        InputAccount account = new InputAccount();
        account.setId(id);
        account.setIdentifier(SharedPreferenceManager.getIdentifier(context));
        account.setFrom(from);
        account.setTo(to);
        account.setPointOneLat(points.get(FIRST_CLICK).latitude);
        account.setPointOneLng(points.get(FIRST_CLICK).longitude);
        account.setPointTwoLat(points.get(SECOND_CLICK).latitude);
        account.setPointTwoLng(points.get(SECOND_CLICK).longitude);
        return gson.toJson(account);
    }

    private String translateToMinJSON(int id, int companionId, int type) {
        Gson gson = new Gson();
        InputAccount account = new InputAccount();
        account.setId(id);
        account.setIdentifier(SharedPreferenceManager.getIdentifier(context));
        account.setCompanion_id(companionId);
        account.setType(type);
        return gson.toJson(account);
    }

    private String translateToMinJSON(int id) {
        Gson gson = new Gson();
        InputAccount account = new InputAccount();
        account.setId(id);
        account.setIdentifier(SharedPreferenceManager.getIdentifier(context));
        return gson.toJson(account);
    }

    private String translateToMinJSON(int id, boolean isIdentifier) {
        Gson gson = new Gson();
        InputAccount account = new InputAccount();
        account.setId(id);
        SharedPreferenceManager.getGoogleIdentifier(account, context);
        if(isIdentifier) {
            account.setIdentifier(SharedPreferenceManager.getIdentifier(context));
        }
        return gson.toJson(account);
    }

    private void refreshMarker(int element, OutputAccount account,
                               List<Integer> peopleIdentifiers) {
        connectionStateListener.redirectMarkerRefreshing(MARKER_REFRESHING,
                new InfoAccount(peopleIdentifiers.get(element),
                        account.getPeopleLatitudes().get(element),
                        account.getPeopleLongitudes().get(element),
                        account.getPeopleStatuses().get(element)));
    }

    private void addMarker(int element, OutputAccount account,
                           List<Integer> peopleIdentifiers) {
        connectionStateListener.redirectMarkerAddition(MARKER_ADDITION,
                new InfoAccount(peopleIdentifiers.get(element),
                        account.getName().get(element),
                        account.getAge().get(element),
                        account.getHobbies().get(element),
                        account.getPhotos().get(element),
                        account.getPeopleLatitudes().get(element),
                        account.getPeopleLongitudes().get(element),
                        account.getPeopleStatuses().get(element)));
    }

    private void deleteMarker(Set<Integer> markers) {
        connectionStateListener.redirectMarkerDeleting(MARKER_DELETING,
                markers);
    }

    public void timeExceed(int id, int companionId){
        String json = translateToMinJSON(id, companionId);
        sendHttpRequest(json, DEFAULT_ADDRESS);
    }

    public void sendFirstRequest(int id){
        if(SharedPreferenceManager.getIdentifier(context) != NO_IDENTIFIER) {
            String json = translateToMinJSON(id);
            sendHttpRequest(json, DEFAULT_ADDRESS);
        }
    }

    public void doAlertNegative(int id){
        Gson gson = new Gson();
        InputAccount account = getDefaultInputAccount(id, -1,
                markerReceiver.getLocation());
        getAccountData(account);
        SharedPreferenceManager.getGoogleIdentifier(account, context);
        String json = gson.toJson(account);
        String outputJSON = sendHttpRequest(json, DEFAULT_ADDRESS);
        OutputAccount outputAccount = gson.fromJson(outputJSON, OutputAccount.class);
        SharedPreferenceManager.saveIdentifier(outputAccount.getIdentifier(), context);
    }

    public boolean doAlertPositive(int id){
        boolean result;
        String json = translateToMinJSON(id, false);
        String outputJSON = sendHttpRequest(json, DEFAULT_ADDRESS);
        if(!outputJSON.equals(DEFAULT_OUTPUT_JSON)) {
            Gson gson = new Gson();
            OutputAccount outputAccount = gson.fromJson(outputJSON, OutputAccount.class);
            SharedPreferenceManager.saveIdentifier(outputAccount.getIdentifier(), context);
            new FileManager(context.getString(R.string.user_data_file))
                    .saveUserDataFromServer(outputAccount, context);
            result = true;
        }
        else{
            result = false;
        }
        return result;
    }

    public OutputAccount sendAccountRequest(int id, List<LatLng> points, String from,
                                   String to){
        if(SharedPreferenceManager.getIdentifier(context) != NO_IDENTIFIER) {
            String json = translateToMinJSON(id, points, from, to);
            Gson gson = new Gson();
            return gson.fromJson(sendHttpRequest(json, SPECIAL_ADDRESS), OutputAccount.class);
        }
        return null;
    }

    private boolean hasInternetConnection(Context context) {
        boolean connection = false;
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(MainActivity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivity.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (networkInfo != null && networkInfo.isConnected()) {
            connection = true;
        }
        networkInfo = connectivity.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (networkInfo != null && networkInfo.isConnected()) {
            connection = true;
        }
        return connection;
    }

}
