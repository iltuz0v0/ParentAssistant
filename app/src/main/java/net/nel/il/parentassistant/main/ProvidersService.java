package net.nel.il.parentassistant.main;

import android.app.Activity;
import android.content.Context;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import net.nel.il.parentassistant.AlertDialogService;
import net.nel.il.parentassistant.R;
import net.nel.il.parentassistant.ToastManager;

public class ProvidersService {

    public boolean trackGPS(Activity context,
                            LocationManager locationManager, boolean isPermission) {
        boolean tracing = false;
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && isPermission) {
            new AlertDialogService().createGPSRequest(context);
        }else if(isPermission){
            tracing = true;
        }
        return tracing;
    }

    public boolean search(Activity context,
                          LocationManager locationManager, boolean isPermission) {
        boolean networkConnection = false;
        if (trackGPS(context, locationManager, isPermission) && hasInternetConnection(context)) {
            networkConnection = true;
        } else if (!hasInternetConnection(context)) {
            ToastManager.showToast(context.getResources().getString(R.string.toast_turn_on_internet), context);

        }
        return networkConnection;
    }

    @SuppressWarnings("all")
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
