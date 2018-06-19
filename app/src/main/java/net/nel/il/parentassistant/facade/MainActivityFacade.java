package net.nel.il.parentassistant.facade;

import android.app.NotificationManager;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;

import net.nel.il.parentassistant.main.MajorHandler;

public class MainActivityFacade {

    public LocationManager locationManager;

    public MajorHandler majorHandler;

    public int cancel;

    public int search;

    public int location;

    public int close;

    public int messaging;

    public ColorDrawable color;

    public int wait;

    public int requestFrom;

    public int messageIconState;

    public int walkTypeLayout;

    public int walkType;

    public int cId;

    public NotificationManager notificationManager;


    public MainActivityFacade(LocationManager locationManager, MajorHandler majorHandler, NotificationManager notificationManager) {
        this.notificationManager = notificationManager;
        this.locationManager = locationManager;
        this.majorHandler = majorHandler;
    }

    public MainActivityFacade() {
    }
}
