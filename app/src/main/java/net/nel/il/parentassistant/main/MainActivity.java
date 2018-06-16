package net.nel.il.parentassistant.main;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;

import net.nel.il.parentassistant.AlertDialogService;
import net.nel.il.parentassistant.Messaging.Messaging;
import net.nel.il.parentassistant.Messaging.MessagingFragment;
import net.nel.il.parentassistant.ToastManager;
import net.nel.il.parentassistant.facade.MainActivityFacade;
import net.nel.il.parentassistant.location.GPSManager;
import net.nel.il.parentassistant.model.InfoAccount;
import net.nel.il.parentassistant.model.OutputAccount;
import net.nel.il.parentassistant.network.NetworkClient;
import net.nel.il.parentassistant.R;
import net.nel.il.parentassistant.account.AccountActivity;
import net.nel.il.parentassistant.network.NetworkClientMessaging;
import net.nel.il.parentassistant.notification.CustomNotificationManager;
import net.nel.il.parentassistant.schedule.EventQueue;
import net.nel.il.parentassistant.schedule.ScheduleFragment;
import net.nel.il.parentassistant.settings.SettingsActivity;
import net.nel.il.parentassistant.settings.SharedPreferenceManager;

import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        Handler.Callback, MessagingFragment.MessagingDialogCallback,
        ScheduleFragment.ScheduleFragmentCallback{

    public static final int locationPermissionsRequestCode = 10;

    public final int internetPermissionRequestCode = 11;

    public static final int gpsEnableRequestCode = 12;

    public static final int googleAccountRequestCode = 15;

    public final int radiusChangeRequestCode = 13;

    public final int accountChangeRequestCode = 14;

    public static boolean isPermissionLocation = false;

    public static boolean isPermissionInternet = false;

    public static final int ONE_MESSAGE_ICON = 14;

    public static final int MANY_MESSAGES_ICON = 15;

    private LocationManager locationManager;

    private Handler mainActivityHandler;

    private MajorHandler majorHandler;

    private AlertDialogService alertDialogService;

    private GoogleSignInClient client;

    private FloatingActionButton location;

    private FloatingActionButton search;

    private MapFragment map;

    private Button cancel;

    private ProgressBar wait;

    private MenuItem accountMenuItem;

    private MenuItem scheduleMenuItem;

    private MenuItem settingsMenuItem;

    private MenuItem signedInMenuItem;

    private LinearLayout requestWindow;

    private Button accept;

    private Button reject;

    private ImageView close;

    private FloatingActionButton message;

    private FrameLayout mainLayout;

    private LinearLayout signInLinearLayout;

    private SignInButton signInButton;

    private LinearLayout walkTypeLayout;

    private ImageButton buggyButton;

    private ImageButton walkButton;

    private ImageView walkFrom;

    private int messageIconState = R.drawable.message;

    private boolean isSignIn = true;

    private static final Integer BUGGY = 1;

    private static final Integer WALK = 2;

    private int currentWalkState;

    private int cId;

    private Dialog dialog = null;

    private NotificationManager manager;

    private CustomNotificationManager customNotificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        variablesInitialization(savedInstanceState);
        signIn(false);
        if(savedInstanceState == null){
            checkPermissions();
            //majorHandler.beginTracingGPS(this);
        }
        else{
            refreshInstance();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        int id = intent.getIntExtra(getString(R.string.intent_extra_id), 0);
        int type = intent.getIntExtra(getString(R.string.intent_extra_type), 1);
        showRequestFromNotification(id, type);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case gpsEnableRequestCode:
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    //majorHandler.beginTracingGPS(this);
                }
                break;
            case radiusChangeRequestCode:
                majorHandler.changeRadius(getApplicationContext());
                break;
            case googleAccountRequestCode:
                if(resultCode == RESULT_OK) {
                    checkGoogleAccount(data);
                }
                else{
                    signIn(false);
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        accountMenuItem = menu.findItem(R.id.account);
        scheduleMenuItem = menu.findItem(R.id.schedule);
        settingsMenuItem = menu.findItem(R.id.settings);
        signedInMenuItem = menu.findItem(R.id.signed_in);
        if(!isSignIn) {
            optionsMenuState(false);
        }
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
            case R.id.signed_in:
                client.signOut();
                signIn(false);
                break;
            case R.id.schedule:
                majorHandler.openScheduleFragment(this);
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.search:
                //majorHandler.beginSearching(this);
                break;
            case R.id.cancel:
                cancelRequest();
                break;
            case R.id.accept:
                acceptRequest();
                break;
            case R.id.reject:
                rejectRequest();
                break;
            case R.id.close:
                closeRequest();
                setClose();
                break;
            case R.id.messaging:
                majorHandler.openMessagingDialog(this);
                break;
            case R.id.sign_in_button:
                Intent signInIntent = client.getSignInIntent();
                startActivityForResult(signInIntent, googleAccountRequestCode);
                break;
            case R.id.buggy_button:
                sendCompanionRequest(BUGGY);
                break;
            case R.id.walk_button:
                sendCompanionRequest(WALK);
                break;
            case R.id.location:
                majorHandler.findCurrentLocation();
                break;
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        majorHandler.recreateHandler(mainActivityHandler);
    }

    private void checkPermissions() {
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
//                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, locationPermissionsRequestCode);
//        } else {
//            isPermissionLocation = true;
//        }
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, internetPermissionRequestCode);
//        } else {
//            isPermissionInternet = true;
//        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case locationPermissionsRequestCode:
                if (permissions.length > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    isPermissionLocation = true;
                   // majorHandler.beginTracingGPS(this);
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
    public Object onRetainCustomNonConfigurationInstance() {
        MainActivityFacade facade = new MainActivityFacade(locationManager,
                majorHandler, manager);
        facade.cancel = cancel.getVisibility();
        facade.messaging = message.getVisibility();
        facade.search = search.getVisibility();
        facade.location = location.getVisibility();
        facade.close = close.getVisibility();
        facade.color = (ColorDrawable) mainLayout.getForeground();
        facade.wait = wait.getVisibility();
        facade.messageIconState = messageIconState;
        facade.requestFrom = requestWindow.getVisibility();
        facade.walkTypeLayout = walkTypeLayout.getVisibility();
        facade.walkType = currentWalkState;
        facade.cId = cId;
        if(dialog != null){
            dialog.dismiss();
        }
        majorHandler.nullHandler();
        majorHandler.setNegativeAccountTrying();
        mainActivityHandler = null;
        return facade;
    }

    @Override
    public boolean handleMessage(Message message) {
        handleMessageChangedInstance(message);
        handleMessageRedirection(message);
        return true;
    }

    @SuppressWarnings("all")
    public void handleMessageRedirection(Message message){
        switch (message.what){
            case NetworkClientMessaging.MARKER_ADDITION:
                majorHandler.addMarker((InfoAccount) message.obj);
                break;
            case NetworkClientMessaging.MARKER_REFRESHING:
                majorHandler.refreshMarker((InfoAccount) message.obj);
                break;
            case NetworkClientMessaging.MARKER_DELETING:
                majorHandler.deleteMarkers((Set<Integer>) message.obj);
                break;
            case RouteClient.HANDLER_DRAW_ROUTE:
                majorHandler.drawRoute(message.obj);
                break;
            case NetworkClient.ROUTE_REBUILDING:
                majorHandler.rebuildRoute(message.arg1);
                break;
            case NetworkClient.ROUTE_REMOVING:
                majorHandler.removeRoute();
                break;
            case NetworkClient.COMPANION_IS_OFFLINE:
                majorHandler.setClose();
                showOfflineCompanion();
                break;
            case NetworkClient.NO_CONNECTION:
                showNoConnection();
                majorHandler.unblockEverything();
                break;
            case MajorHandler.REFRESH_CHAT:
                majorHandler.refreshChat();
                break;
            case NetworkClient.EVENT_REQUEST:
                majorHandler.sendAccountsToScheduleFragment(message.obj);
                break;
            case NetworkClient.MESSAGE_ANSWER:
                majorHandler.sendBadMessageState(message.arg1, message.arg2);
                break;
            case NetworkClient.TRYING_TO_RETURN_OLD_ACCOUNT:
                dialog = alertDialogService.createOldAcountReturningDialog(this,
                        getString(R.string.returning_old_account));
                break;
        }
    }

    public void handleMessageChangedInstance(Message message){
        switch (message.what) {
            case MajorHandler.IS_REQUEST_TO:
                chooseWalkType(message.arg1);
                break;
            case NetworkClient.REQUEST_ACCEPTED:
                showRequestAcception();
                break;
            case NetworkClient.REQUEST_REJECTED:
                showRequestRejection();
                majorHandler.unblockEverything();
                break;
            case NetworkClient.REQUEST_BROKEN:
                showBreakRequest();
                majorHandler.unblockEverything();
                break;
            case NetworkClient.TIME_EXCEED:
                showTimeExceedMessage();
                majorHandler.unblockEverything();
                break;
            case NetworkClient.IS_REQUEST_FROM:
                showRequestFromMessage(message.arg1, message.arg2);
                break;
            case ONE_MESSAGE_ICON:
                drawMessagingState(R.drawable.message_one);
                break;
            case MANY_MESSAGES_ICON:
                drawMessagingState(R.drawable.message_many);
                break;
            case GPSManager.NO_LOCATION_PERMISSION:
                checkPermissions();
                break;
            case NetworkClient.BUSY_REQUEST:
                showBusyRequest();
                break;
            case InfoWindow.SHOW_INFO_WINDOW:
                showCompanionRequest(message);
                break;
            case NetworkClient.NO_INTERNET:
                dialog = alertDialogService.createCommunicationStateDialog(this,
                        getString(R.string.no_internet));
                break;
            case NetworkClient.ACCOUNT_UPLOADED:
                doAccountUploadedState((boolean)message.obj);
                break;
        }
    }

    private void doAccountUploadedState(boolean isUploaded){
        if(isUploaded){
            ToastManager.showToast(getString(R.string.alert_ok), this);
        }
        else{
            ToastManager.showToast(getString(R.string.failed), this);
        }
        changeConstantButtonStates(true);
        optionsMenuState(true);
        mainLayout.setForeground(new ColorDrawable(
                getResources().getColor(R.color.transparent)));
    }

    private void closeRequest(){
        majorHandler.setClose();
    }

    private void sendCompanionRequest(int type){
        walkTypeLayout.setVisibility(View.INVISIBLE);
        majorHandler.sendCompanionRequest(cId, type);
        showCancel();
    }

    public void chooseWalkType(int companionId){
        walkTypeLayout.setVisibility(View.VISIBLE);
        cId = companionId;
    }

    private void showBusyRequest(){
        cancel.setVisibility(View.INVISIBLE);
        dialog = alertDialogService.createCommunicationStateDialog(this,
                getString(R.string.busy_request));
        unableWaiting();
    }

    private void cancelRequest(){
        majorHandler.changeMarkerColorToDefault(NetworkClient.companionId);
        majorHandler.clearMessages();
        message.setVisibility(View.INVISIBLE);
        majorHandler.finishRequest();
        cancel.setVisibility(View.INVISIBLE);
        wait.setVisibility(View.INVISIBLE);
    }

    private void acceptRequest(){
        majorHandler.changeMarkerColorToDefault(NetworkClient.companionId);
        manager.cancelAll();
        majorHandler.clearMessages();
        message.setVisibility(View.VISIBLE);
        requestWindow.setVisibility(View.INVISIBLE);
        cancel.setVisibility(View.VISIBLE);
        majorHandler.setAccept();
    }

    private void rejectRequest(){
        majorHandler.changeMarkerColorToDefault(NetworkClient.companionId);
        manager.cancelAll();
        majorHandler.setReject();
        requestWindow.setVisibility(View.INVISIBLE);
    }

    private void showOfflineCompanion(){
        cancel.setVisibility(View.INVISIBLE);
        dialog = alertDialogService.createCommunicationStateDialog(this,
                getString(R.string.companion_is_offline));
    }

    private void drawMessagingState(int drawableResource){
        this.message.setImageResource(drawableResource);
        messageIconState = drawableResource;
    }

    private void showCancel() {
        mainLayout.setForeground(new ColorDrawable(
                getResources().getColor(R.color.main_activity_foreground)));
        wait.setVisibility(View.VISIBLE);
        close.setVisibility(View.VISIBLE);
    }

    private void showRequestAcception() {
        message.setImageResource(R.drawable.message);
        message.setVisibility(View.VISIBLE);
        cancel.setVisibility(View.VISIBLE);
        unableWaiting();
    }

    private void showRequestRejection() {
        cancel.setVisibility(View.INVISIBLE);
        dialog = alertDialogService.createCommunicationStateDialog(this,
                getString(R.string.subscriber_rejected));
        unableWaiting();
    }

    private void showNoConnection() {
        cancel.setVisibility(View.INVISIBLE);
        dialog = alertDialogService.createCommunicationStateDialog(this,
                getString(R.string.subscriber_not_connection));
        unableWaiting();
    }

    private void showBreakRequest() {
        message.setVisibility(View.INVISIBLE);
        cancel.setVisibility(View.INVISIBLE);
        wait.setVisibility(View.INVISIBLE);
        dialog = alertDialogService.createCommunicationStateDialog(this,
                getString(R.string.subscriber_broke_connection));
    }

    private void showTimeExceedMessage(){
        cancel.setVisibility(View.INVISIBLE);
        dialog = alertDialogService.createCommunicationStateDialog(this,
                getString(R.string.subscriber_doesnt_answer));
        unableWaiting();
    }

    private void showRequestFromMessage(int id, int type) {
        customNotificationManager.createRequestNotification(manager, id, type, this);
        majorHandler.blockEverything();
        int result = majorHandler.illuminateMarker(id);
        if(result == MapManager.IS_DESTINATION_MARKER){
            requestWindow.setVisibility(View.VISIBLE);
            if(type == BUGGY){
                walkFrom.setImageResource(R.drawable.buggy);
                currentWalkState = R.drawable.buggy;
            }
            else if(type == WALK){
                walkFrom.setImageResource(R.drawable.walk);
                currentWalkState = R.drawable.walk;
            }
        }
        else{
            majorHandler.setNotAvailable();
        }
    }

    private void showRequestFromNotification(int id, int type){
        majorHandler.blockEverything();
        int result = majorHandler.illuminateMarker(id);
        if(result == MapManager.IS_DESTINATION_MARKER){
            majorHandler.buildPossibleRoute(id);
            requestWindow.setVisibility(View.VISIBLE);
            if(type == BUGGY){
                walkFrom.setImageResource(R.drawable.buggy);
                currentWalkState = R.drawable.buggy;
            }
            else if(type == WALK){
                walkFrom.setImageResource(R.drawable.walk);
                currentWalkState = R.drawable.walk;
            }
        }
        else {
            majorHandler.setNotAvailable();
        }
    }

    private void setClose(){
        cancel.setVisibility(View.INVISIBLE);
        unableWaiting();
    }

    private void variablesInitialization(Bundle savedInstanceState) {
        map = (MapFragment) getFragmentManager().findFragmentById(R.id.mapFragment);
        //mainActivityHandler = new Handler(this);
        if (savedInstanceState == null) {
            firstInitialization();
        }
        customNotificationManager = new CustomNotificationManager();
        walkFrom = (ImageView) findViewById(R.id.walk_from_image_view);
        walkTypeLayout = (LinearLayout) findViewById(R.id.walkTypeLayout);
        buggyButton = (ImageButton) findViewById(R.id.buggy_button);
        //buggyButton.setOnClickListener(this);
        walkButton = (ImageButton) findViewById(R.id.walk_button);
        //walkButton.setOnClickListener(this);
        signInLinearLayout = (LinearLayout) findViewById(R.id.sign_in_layout);
        signInButton = findViewById(R.id.sign_in_button);
        //signInButton.setOnClickListener(this);
        message = (FloatingActionButton) findViewById(R.id.messaging);
        //
        message.setVisibility(View.VISIBLE);
        //
        //message.setOnClickListener(this);
        alertDialogService = new AlertDialogService();
        mainLayout = (FrameLayout) findViewById(R.id.main_layout);
        close = (ImageView) findViewById(R.id.close);
        //close.setOnClickListener(this);
        requestWindow = (LinearLayout) findViewById(R.id.is_request_window);
        //requestWindow.setOnClickListener(this);
        accept = (Button) findViewById(R.id.accept);
        //accept.setOnClickListener(this);
        reject = (Button) findViewById(R.id.reject);
        //reject.setOnClickListener(this);
        cancel = (Button) findViewById(R.id.cancel);
        //cancel.setOnClickListener(this);
        location = (FloatingActionButton) findViewById(R.id.location);
        //location.setOnClickListener(this);
        search = (FloatingActionButton) findViewById(R.id.search);
        //search.setOnClickListener(this);
        wait = (ProgressBar) findViewById(R.id.request_in_progress);
    }

    private void firstInitialization(){
        manager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        majorHandler = new MajorHandler(getApplicationContext(), locationManager,
                map, mainActivityHandler);
    }

    @Override
    public void sendMessage(String message, int position, int finishPosition) {
        majorHandler.sendMessage(message, position, finishPosition);
    }

    @Override
    public Messaging getMessagingObject() {
        return majorHandler.getMessagingObject();
    }

    @Override
    public void setMessagingDialogState(boolean state, MessagingFragment messagingFragment) {
        majorHandler.setMessagingDialogState(state, messagingFragment);
    }

    @Override
    public void block() {
        doBlockState(View.INVISIBLE);
        optionsMenuVisible(false);
    }

    @Override
    public void unblock() {
        doBlockState(View.VISIBLE);
        optionsMenuVisible(true);
    }

    private void doBlockState(int visibility){
        cancel.setVisibility(visibility);
        search.setVisibility(visibility);
        location.setVisibility(visibility);
        message.setVisibility(visibility);
    }

    @Override
    public void setDefaultMessagingIcon() {
        message.setImageResource(R.drawable.message);
    }

    @Override
    public void refreshChatState(MessagingFragment messagingFragment) {
        majorHandler.refreshChatState(messagingFragment);
    }

    @Override
    public Context getAppContext() {
        return getApplicationContext();
    }

    @Override
    public Location getLocation() {
        return majorHandler.getLocation();
    }

    @Override
    public void sendAccountRequest(List<LatLng> points, String from,
                                   String to) {
        majorHandler.sendAccountRequest(points, from, to);
    }

    @Override
    public void sendEventRequest(List<LatLng> points, String from, String to) {
        majorHandler.sendEventRequest(points, from, to);
    }

    @Override
    public EventQueue getEventQueue() {
        return majorHandler.getEventQueue();
    }

    @Override
    public void setScheduleFragmentState(boolean state, ScheduleFragment scheduleFragment) {
        if(message.getVisibility() == View.INVISIBLE){
            cancel.setVisibility(View.INVISIBLE);
        }
        majorHandler.setScheduleFragmentState(state, scheduleFragment);
    }

    public void refreshInstance(){
        MainActivityFacade facade = (MainActivityFacade)
                getLastCustomNonConfigurationInstance();
        manager = facade.notificationManager;
        locationManager = facade.locationManager;
        majorHandler = facade.majorHandler;
        search.setVisibility(facade.search);
        location.setVisibility(facade.location);
        message.setVisibility(facade.messaging);
        message.setImageResource(facade.messageIconState);
        cancel.setVisibility(facade.cancel);
        close.setVisibility(facade.close);
        mainLayout.setForeground(facade.color);
        wait.setVisibility(facade.wait);
        requestWindow.setVisibility(facade.requestFrom);
        walkTypeLayout.setVisibility(facade.walkTypeLayout);
        walkFrom.setImageResource(facade.walkType);
        currentWalkState = facade.walkType;
        cId = facade.cId;
        majorHandler.refreshMap(map);
        if(requestWindow.getVisibility() == View.VISIBLE){
            majorHandler.changeMarkerColorToSpecial(NetworkClient.companionId);
        }
    }

    private void showCompanionRequest(Message message){
        InfoWindow infoWindow = new InfoWindow();
        infoWindow.createCompanionRequestWindow(this, message.obj,
                message.arg1, message.arg2, majorHandler);
    }

    private void signIn(boolean isRepeat){
        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestId()
                .requestEmail()
                .build();
        client = GoogleSignIn.getClient(this, gso);
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if(account == null){
            if(isRepeat){
                changeConstantButtonStates(false);
                optionsMenuState(false);
            }
            isSignIn = false;
            changeConstantButtonStates(false);
            invalidateOptionsMenu();
            signInLinearLayout.setVisibility(View.VISIBLE);
        }
    }

    private void checkGoogleAccount(Intent data){
        signInLinearLayout.setVisibility(View.INVISIBLE);
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            changeConstantButtonStates(true);
            optionsMenuState(true);
            GoogleSignInAccount account = task.getResult(ApiException.class);
            boolean result = SharedPreferenceManager
                    .saveGoogleIdentifier(getApplicationContext(), account);
            if(result){
                SharedPreferenceManager.accountInformationUpdated(getApplicationContext());
            }
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }

    private void changeConstantButtonStates(boolean state){
        location.setClickable(state);
        search.setClickable(state);
    }

    private void optionsMenuState(boolean state){
        if(accountMenuItem != null) {
            accountMenuItem.setEnabled(state);
            scheduleMenuItem.setEnabled(state);
            settingsMenuItem.setEnabled(state);
            signedInMenuItem.setVisible(state);
        }
    }

    private void optionsMenuVisible(boolean state){
        if(accountMenuItem != null) {
            accountMenuItem.setVisible(state);
            scheduleMenuItem.setVisible(state);
            settingsMenuItem.setVisible(state);
            signedInMenuItem.setVisible(state);
        }
    }

    private void unableWaiting() {
        wait.setVisibility(View.INVISIBLE);
        close.setVisibility(View.INVISIBLE);
        mainLayout.setForeground(new ColorDrawable(
                getResources().getColor(R.color.transparent)));
    }

    public void doAlertPositive(){
        changeConstantButtonStates(false);
        optionsMenuState(false);
        mainLayout.setForeground(new ColorDrawable(
                getResources().getColor(R.color.main_activity_foreground)));
        majorHandler.doAlertPositive();
    }

    public void doAlertNegative(){
        majorHandler.doAlertNegative();
    }

}

