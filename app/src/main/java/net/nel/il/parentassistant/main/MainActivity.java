package net.nel.il.parentassistant.main;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.maps.MapFragment;

import net.nel.il.parentassistant.network.NetworkClient;
import net.nel.il.parentassistant.R;
import net.nel.il.parentassistant.account.AccountActivity;
import net.nel.il.parentassistant.settings.SettingsActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        Handler.Callback {

    public static final int locationPermissionsRequestCode = 10;

    public final int internetPermissionRequestCode = 11;

    public static final int gpsEnableRequestCode = 12;

    public final int radiusChangeRequestCode = 13;

    public final int accountChangeRequestCode = 14;

    public static boolean isPermissionLocation = false;

    public static boolean isPermissionInternet = false;

    private LocationManager locationManager;

    private FloatingActionButton search;

    private MapFragment map;

    private MajorHandler majorHandler;

    private Handler mainActivityHandler;

    private Button cancel;

    private ProgressBar wait;

    private LinearLayout isRequestWindow;

    private Button accept;

    private Button reject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        variablesInitialization();
        checkPermissions();
        majorHandler.beginTracingGPS(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case gpsEnableRequestCode:
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    majorHandler.beginTracingGPS(this);
                }
                break;
            case radiusChangeRequestCode:
                majorHandler.changeRadius(this);
                break;
            case accountChangeRequestCode:
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                Intent settings = new Intent(this, SettingsActivity.class);
                startActivityForResult(settings, radiusChangeRequestCode);
                break;
            case R.id.account:
                Intent account = new Intent(this, AccountActivity.class);
                startActivityForResult(account, accountChangeRequestCode);
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.search:
                majorHandler.beginSearching(this);
                break;
            case R.id.cancel:
                majorHandler.endRequest();
                cancel.setVisibility(View.INVISIBLE);
                wait.setVisibility(View.INVISIBLE);
                break;
            case R.id.accept:
                majorHandler.setAccept();
                isRequestWindow.setVisibility(View.INVISIBLE);
                cancel.setVisibility(View.VISIBLE);
                break;
            case R.id.reject:
                majorHandler.setReject();
                isRequestWindow.setVisibility(View.INVISIBLE);
                break;

        }
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, locationPermissionsRequestCode);
        } else {
            isPermissionLocation = true;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, internetPermissionRequestCode);
        } else {
            isPermissionInternet = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case locationPermissionsRequestCode:
                if (permissions.length > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    isPermissionLocation = true;
                    majorHandler.beginTracingGPS(this);
                }
                break;

            case internetPermissionRequestCode:
                if (permissions.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    isPermissionInternet = true;
                }
                break;
        }
    }

    @Override
    public boolean handleMessage(Message message) {
        switch (message.what) {
            case MajorHandler.IS_REQUEST_TO:
                showCancel();
                break;
            case NetworkClient.REQUEST_ACCEPTED:
                acceptRequest();
                break;
            case NetworkClient.REQUEST_REJECTED:
                rejectRequest();
                break;
            case NetworkClient.REQUEST_BROKEN:
                breakRequest();
                break;
            case NetworkClient.TIME_EXCEED:
                showTimeExceedMessage();
                break;
            case NetworkClient.IS_REQUEST_FROM:
                showRequestFromMessage();
                break;

        }
        return true;
    }

    private void showCancel() {
        wait.setVisibility(View.VISIBLE);
    }

    private void acceptRequest() {
        wait.setVisibility(View.INVISIBLE);
        cancel.setVisibility(View.VISIBLE);
    }

    private void rejectRequest() {
        wait.setVisibility(View.INVISIBLE);
        cancel.setVisibility(View.INVISIBLE);
        Toast.makeText(this, "Human rejected",
                Toast.LENGTH_LONG).show();
    }

    private void breakRequest() {
        cancel.setVisibility(View.INVISIBLE);
        wait.setVisibility(View.INVISIBLE);
        Toast.makeText(this, "Human broken",
                Toast.LENGTH_LONG).show();
    }

    private void showTimeExceedMessage() {
        wait.setVisibility(View.INVISIBLE);
        cancel.setVisibility(View.INVISIBLE);
        Toast.makeText(this, "Human doesn't answer",
                Toast.LENGTH_LONG).show();
    }

    private void showRequestFromMessage() {
        isRequestWindow.setVisibility(View.VISIBLE);
    }

    private void variablesInitialization() {
        isRequestWindow = (LinearLayout) findViewById(R.id.is_request_window);
        isRequestWindow.setOnClickListener(this);
        accept = (Button) findViewById(R.id.accept);
        accept.setOnClickListener(this);
        reject = (Button) findViewById(R.id.reject);
        reject.setOnClickListener(this);
        cancel = (Button) findViewById(R.id.cancel);
        cancel.setOnClickListener(this);
        search = (FloatingActionButton) findViewById(R.id.search);
        search.setOnClickListener(this);
        wait = (ProgressBar) findViewById(R.id.request_in_progress);
        map = (MapFragment) getFragmentManager().findFragmentById(R.id.mapFragment);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mainActivityHandler = new Handler(this);
        majorHandler = new MajorHandler(this, locationManager,
                map, getLayoutInflater(), mainActivityHandler);
    }
}

