package net.nel.il.parentassistant.settings;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferenceManager {

    public void setValue(Context context, int resourceFile,
                         float value, int resourceValue) {
        SharedPreferences.Editor settings = context.getSharedPreferences(
                context.getResources()
                        .getString(resourceFile), Context.MODE_PRIVATE).edit();
        settings.putFloat(context.getResources()
                .getString(resourceValue), value);
        settings.apply();
    }

    public float getValue(Context context, int resourceFile,
                          int resourceValue) {
        SharedPreferences settings = context.getSharedPreferences(
                context.getResources().getString(resourceFile),
                Context.MODE_PRIVATE);
        return settings.getFloat(context.getResources()
                .getString(resourceValue), -1.0f);
    }
}
