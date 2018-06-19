package net.nel.il.parentassistant;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.nel.il.parentassistant.main.MainActivity;

public class AlertDialogService {

    public Dialog createGPSRequest(final Activity context) {
        AlertDialog gpsDialog = null;
        AlertDialog.Builder gpsDialogBuilder = new AlertDialog.Builder(context);
        gpsDialogBuilder.setMessage(context.getResources().getString(R.string.dialog_turn_on_gps));
        gpsDialogBuilder.setPositiveButton(context.getResources().getString(R.string.dialog_turn_on), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                context.startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), MainActivity.gpsEnableRequestCode);
            }
        });
        gpsDialogBuilder.setNegativeButton(context.getResources().getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        gpsDialog = gpsDialogBuilder.create();
        gpsDialog.show();
        return gpsDialog;
    }

    public Dialog createCommunicationStateDialog(Context context, String message) {
        AlertDialog dialog = null;
        AlertDialog.Builder communicationDialogBuilder = new AlertDialog.Builder(context);
        communicationDialogBuilder.setPositiveButton(context.getString(R.string.alert_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        TextView title = new TextView(context);
        title.setText(message);
        int leftPadding = context.getResources().getInteger(R.integer.alert_title_left_padding);
        int topPadding = context.getResources().getInteger(R.integer.alert_title_top_padding);
        title.setPadding(leftPadding, topPadding, leftPadding, topPadding);
        title.setGravity(Gravity.CENTER);
        title.setTextSize(context.getResources().getInteger(R.integer.alert_title_text_size));
        communicationDialogBuilder.setCustomTitle(title);
        dialog = communicationDialogBuilder.create();
        dialog.show();
        fixPositiveButtonPosition(dialog, context);
        return dialog;
    }

    private void fixPositiveButtonPosition(AlertDialog companionDialog, Context context) {
        Button positiveButton = companionDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setBackgroundColor(context.getResources().getColor(R.color.alert_background));
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) positiveButton.getLayoutParams();
        layoutParams.weight = context.getResources().getInteger(R.integer.alert_buttons_weight);
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.rightMargin = context.getResources().getInteger(R.integer.alert_right_margin);
        positiveButton.setLayoutParams(layoutParams);
    }

    public Dialog createOldAcountReturningDialog(final MainActivity context, String text) {
        AlertDialog gpsDialog;
        AlertDialog.Builder gpsDialogBuilder = new AlertDialog.Builder(context);
        gpsDialogBuilder.setMessage(text);
        gpsDialogBuilder.setPositiveButton(context.getResources().getString(R.string.alert_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                context.doAlertPositive();
            }
        });
        gpsDialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                context.doAlertNegative();
            }
        });
        gpsDialogBuilder.setNegativeButton(context.getResources().getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                context.doAlertNegative();
            }
        });
        gpsDialog = gpsDialogBuilder.create();
        gpsDialog.show();
        return gpsDialog;
    }
}
