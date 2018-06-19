package net.nel.il.parentassistant.settings;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import net.nel.il.parentassistant.R;
import net.nel.il.parentassistant.model.InputAccount;

public class SharedPreferenceManager {

    private static final int NO_IDENTIFIER = -1;

    private static final String NO_GOOGLE_IDENTIFIER = "-1";

    public void setValue(Context context, int resourceFile, float value, int resourceValue) {
        SharedPreferences.Editor settings = context.getSharedPreferences(context.getResources().getString(resourceFile), Context.MODE_PRIVATE).edit();
        settings.putFloat(context.getResources().getString(resourceValue), value);
        settings.apply();
    }

    public float getValue(Context context, int resourceFile, int resourceValue) {
        SharedPreferences settings = context.getSharedPreferences(context.getResources().getString(resourceFile), Context.MODE_PRIVATE);
        return settings.getFloat(context.getResources().getString(resourceValue), -1.0f);
    }

    public static int getIdentifier(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(context.getResources().getString(R.string.network_data_file), Context.MODE_PRIVATE);
        return preferences.getInt(context.getString(R.string.shared_id), NO_IDENTIFIER);
    }

    public static void getGoogleIdentifier(InputAccount account, Context context) {
        SharedPreferences preferences = context.getSharedPreferences(context.getResources().getString(R.string.network_data_file), Context.MODE_PRIVATE);
        String googleIdentifier = preferences.getString(context.getString(R.string.google_identifier), NO_GOOGLE_IDENTIFIER);
        account.setGoogleIdentifier(googleIdentifier);
    }

    public static boolean isUpdated(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(context.getResources().getString(R.string.network_data_file), Context.MODE_PRIVATE);
        Boolean result = preferences.getBoolean(context.getString(R.string.shared_update_account), false);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(context.getString(R.string.shared_update_account), false);
        editor.apply();
        return result;
    }

    public static void saveIdentifier(int identifier, Context context) {
        SharedPreferences.Editor preferences = context.
                getSharedPreferences(context.getResources().getString(R.string.network_data_file), Context.MODE_PRIVATE).edit();
        preferences.putInt(context.getString(R.string.shared_id), identifier);
        preferences.apply();
    }

    public static boolean saveGoogleIdentifier(Context context, GoogleSignInAccount account) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getResources().getString(R.string.network_data_file), Context.MODE_PRIVATE);
        String result = sharedPreferences.getString(context.getString(R.string.google_identifier), "-1");
        SharedPreferences.Editor preferences = context.getSharedPreferences(context.getResources().getString(R.string.network_data_file), Context.MODE_PRIVATE).edit();
        preferences.putString(context.getString(R.string.google_identifier), account.getId());
        preferences.apply();
        return !result.equals("-1");
    }

    public static void accountInformationUpdated(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(context.getResources().getString(R.string.network_data_file), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(context.getString(R.string.shared_update_account), true);
        editor.apply();
    }
}
