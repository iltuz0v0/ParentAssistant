package net.nel.il.parentassistant;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;

import net.nel.il.parentassistant.main.MainActivity;

public class AlertDialogService {

    public void createGPSRequest(final Activity context) {
        AlertDialog gpsDialog = null;
        AlertDialog.Builder gpsDialogBuilder = new AlertDialog.Builder(context);
        gpsDialogBuilder.setMessage(
                context.getResources().getString(R.string.dialog_turn_on_gps));
        gpsDialogBuilder.setPositiveButton(
                context.getResources().getString(R.string.dialog_turn_on),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        context.startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), MainActivity.gpsEnableRequestCode);
                    }
                });
        gpsDialogBuilder.setNegativeButton(
                context.getResources().getString(R.string.dialog_cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
        gpsDialog = gpsDialogBuilder.create();
        gpsDialog.show();
    }
}
