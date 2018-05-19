package net.nel.il.parentassistant.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;

import com.google.gson.Gson;

import net.nel.il.parentassistant.FileManager;
import net.nel.il.parentassistant.R;
import net.nel.il.parentassistant.account.AccountDataHandler;
import net.nel.il.parentassistant.interfaces.MarkerStateListener;
import net.nel.il.parentassistant.main.MapManager;
import net.nel.il.parentassistant.model.InfoAccount;
import net.nel.il.parentassistant.model.InputAccount;
import net.nel.il.parentassistant.model.OutputAccount;

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

public class NetworkClientMessaging {

    private AccountDataHandler accountDataHandler;

    private MarkerStateListener markerReceiver;

    private Handler handler;

    private Context context;

    public static final int REFRESH_MARKER = 1;

    public static final int ADD_MARKER = 2;

    public static final int DELETE_MARKER = 3;

    private final int STATUS_AVAILABLE = 1;

    private final int STATUS_UNAVAILABLE = 0;

    private final int NO_IDENTIFIER = -1;

    public NetworkClientMessaging(Context context,
                                  MarkerStateListener markerReceiver,
                                  Handler handler) {
        accountDataHandler = new AccountDataHandler(
                new FileManager(context.getResources()
                        .getString(R.string.user_data_file)));
        this.markerReceiver = markerReceiver;
        this.handler = handler;
        this.context = context;
    }

    public OutputAccount sendDataRequest() {
        Location currentLocation = markerReceiver.getLocation();
        OutputAccount account;
        int identifier = getIdentifier();
        boolean isDataUpdated = isUpdated();
        if (identifier == NO_IDENTIFIER) {
            account = sendDataRequest(NetworkClient.CREATE_ACCOUNT,
                    identifier, currentLocation);
        } else {
            if (isDataUpdated) {
                account = sendDataRequest(NetworkClient.UPDATE,
                        identifier, currentLocation);
            } else {
                account = sendDataRequest(NetworkClient.GET_DATA, identifier,
                        currentLocation);
            }
        }
        return account;
    }

    private OutputAccount sendDataRequest(int id,
                                          int identifier, Location currentLocation) {
        String json = translateToJSON(id, identifier, currentLocation);
        System.out.println("input json " + json);
        String outputJson = sendHttpRequest(json);
        System.out.println("output json" + outputJson);
        OutputAccount account = translateFromJson(outputJson);
        if(account.getId() == NetworkClient.CREATE_ACCOUNT){
            saveIdentifier(account.getIdentifier());
        }
        if(account.getId() == NetworkClient.GET_DATA){
            outputServerData(account);
        }
        return account;
    }

    private String translateToJSON(int id,
                                   int identifier, Location currentLocation) {
        Gson gson = new Gson();
        InputAccount account = new InputAccount(id, identifier,
                (float) (MapManager.defaultCircleRadius * MapManager.courseMeter),
                currentLocation.getLatitude(),
                currentLocation.getLongitude());
        account.setCompanion_id(NetworkClient.companionId);
        if (id == NetworkClient.CREATE_ACCOUNT || id == NetworkClient.UPDATE) {
            getAccountData(account);
        }
        if (id == NetworkClient.GET_DATA) {
            getSentPeopleIdentifiers(account);
        }
        return gson.toJson(account);
    }

    private OutputAccount translateFromJson(String outputJson) {
        Gson gson = new Gson();
        return gson.fromJson(outputJson, OutputAccount.class);
    }

    private void outputServerData(OutputAccount account) {
        List<Integer> peopleIdentifiers = account.getPeopleIdentifiers();
        List<Integer> markers = markerReceiver.getPeopleIdentifiers();
        for (int element = 0; element < peopleIdentifiers.size(); element++) {
            if (markers.remove(peopleIdentifiers.get(element))) {
                refreshMarker(element, account, peopleIdentifiers);
            } else {
                addMarker(element, account, peopleIdentifiers);
            }
        }
        if (markers.size() > 0) {
            deleteMarker(markers);
        }
    }

    private void getAccountData(InputAccount account) {
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
            photos.add(encodeBitmap((Bitmap) dataElement.get(mapTags[mapTagIndex])));
            mapTagIndex++;
            names.add((String) dataElement.get(mapTags[mapTagIndex]));
            mapTagIndex++;
            ages.add((String) dataElement.get(mapTags[mapTagIndex]));
            mapTagIndex++;
            hobbies.add((String) dataElement.get(mapTags[mapTagIndex]));
            mapTagIndex = 0;
        }
        account.setName(names);
        account.setAge(ages);
        account.setHobbies(hobbies);
        account.setPhotos(photos);
    }

    private void getSentPeopleIdentifiers(InputAccount account) {
        List<Integer> identifiers = getPeopleIdentifiers();
        account.setPeopleIdentifiers(identifiers);
    }

    private String encodeBitmap(Bitmap bitmap) {
        byte[] byteArray = null;
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byteArray = stream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private int getIdentifier() {
        SharedPreferences preferences = context.getSharedPreferences(
                context.getResources().getString(R.string.network_data_file),
                Context.MODE_PRIVATE);
        return preferences.getInt(context.getString(R.string.shared_id), NO_IDENTIFIER);
    }

    private boolean isUpdated() {
        SharedPreferences preferences = context.getSharedPreferences(
                context.getResources().getString(R.string.network_data_file),
                Context.MODE_PRIVATE);
        Boolean result = preferences.getBoolean(context
                .getString(R.string.shared_update_account), false);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(context
                .getString(R.string.shared_update_account), false);
        editor.apply();
        return result;
    }

    private List<Integer> getPeopleIdentifiers() {
        return markerReceiver.getPeopleIdentifiers();
    }

    private String sendHttpRequest(final String json) {
        final StringBuilder outputJson = new StringBuilder();
        BufferedReader reader = null;
        BufferedOutputStream writer = null;
        try {
            URL url = new URL("https://mthrsassistant.herokuapp.com/");
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
            sendHttpRequest(json);
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

    public void requestTo(int id, int companionId) {
        String json = translateToMinJSON(id, companionId);
        sendHttpRequest(json);
    }

    public void acceptTo(int id, int companionId) {
        String json = translateToMinJSON(id, companionId);
        sendHttpRequest(json);
    }

    public void rejectTo(int id, int companionId) {
        String json = translateToMinJSON(id, companionId);
        System.out.println("JISON2 " + json);
        sendHttpRequest(json);
    }

    public void breakTo(int id, int companionId) {
        String json = translateToMinJSON(id, companionId);
        System.out.println("JISON" + json);
        sendHttpRequest(json);
    }

    private String translateToMinJSON(int id, int companionId) {
        Gson gson = new Gson();
        InputAccount account = new InputAccount();
        account.setId(id);
        account.setIdentifier(getIdentifier());
        account.setCompanion_id(companionId);
        return gson.toJson(account);
    }

    private void saveIdentifier(Integer identifier) {
        SharedPreferences.Editor preferences = context.
                getSharedPreferences(context.getResources().getString(R.string.network_data_file),
                        Context.MODE_PRIVATE).edit();
        preferences.putInt(context.getString(R.string.shared_id), identifier);
        preferences.apply();
    }

    private void refreshMarker(int element, OutputAccount account,
                               List<Integer> peopleIdentifiers) {
        Message message = handler.obtainMessage();
        message.what = REFRESH_MARKER;
        message.obj = new InfoAccount(peopleIdentifiers.get(element),
                account.getPeopleLatitudes().get(element),
                account.getPeopleLongitudes().get(element),
                account.getPeopleStatuses().get(element));
        handler.sendMessage(message);
    }

    private void addMarker(int element, OutputAccount account,
                           List<Integer> peopleIdentifiers) {
        Message message = handler.obtainMessage();
        message.what = ADD_MARKER;
        InfoAccount infoAccount;
        if(account.getName().size() > element){
            infoAccount = new InfoAccount(peopleIdentifiers.get(element),
                    account.getName().get(element),
                    account.getAge().get(element),
                    account.getHobbies().get(element),
                    account.getPhotos().get(element),
                    account.getPeopleLatitudes().get(element),
                    account.getPeopleLongitudes().get(element),
                    account.getPeopleStatuses().get(element));
        }
        else{
            infoAccount = new InfoAccount(peopleIdentifiers.get(element),
                    "name", "age", "hobby", "photo",
                    account.getPeopleLatitudes().get(element),
                    account.getPeopleLongitudes().get(element),
                    account.getPeopleStatuses().get(element));
        }
        message.obj = infoAccount;
        handler.sendMessage(message);
    }

    private void deleteMarker(List<Integer> markers) {
        Message message = handler.obtainMessage();
        message.what = DELETE_MARKER;
        message.obj = markers;
        handler.sendMessage(message);
    }
}
