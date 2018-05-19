package net.nel.il.parentassistant.main;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import net.nel.il.parentassistant.R;
import net.nel.il.parentassistant.model.InfoAccount;

import java.util.List;

public class InfoWindow {

    private LayoutInflater layoutInflater;

    private Context context;

    private Handler handler;

    InfoWindow(LayoutInflater layoutInflater,
               Context context, Handler handler) {
        this.layoutInflater = layoutInflater;
        this.context = context;
        this.handler = handler;
    }

    public void createCompanionRequestWindow(List<InfoAccount> accounts,
                                       final int companionId) {
        View view = createView(accounts);
        AlertDialog.Builder companionDialogBuilder = new AlertDialog.Builder(context,
                R.style.InfoWindowAlertDialog);
        companionDialogBuilder.setTitle(R.string.alert_title);
        companionDialogBuilder.setView(view);
        companionDialogBuilder.setPositiveButton(R.string.alert_request,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        companionRequest(companionId);
                    }
                });
        companionDialogBuilder.setNegativeButton(R.string.alert_cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
        AlertDialog companionDialog = companionDialogBuilder.create();
        companionDialog.show();
        fixButtonsPosition(companionDialog);
    }

    private View createView(List<InfoAccount> accounts) {
        View view = layoutInflater.inflate(R.layout.marker_info, null);
        ScrollView mainView = (ScrollView) view;
        LinearLayout.LayoutParams layoutParams = getWindowLayoutParams();
        LinearLayout subMainView = (LinearLayout) mainView.getChildAt(0);
        subMainView.setId(R.id.info_scroll_linear);
        for (InfoAccount account : accounts) {
            fillNote(subMainView, layoutParams, account);
        }
        return view;
    }

    private void companionRequest(int companionId) {
        Message message = handler.obtainMessage();
        message.what = MapManager.COMPANION_REQUEST;
        message.arg1 = companionId;
        handler.sendMessage(message);
    }

    private void fixButtonsPosition(AlertDialog companionDialog) {
        Button negativeButton = companionDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams)
                negativeButton.getLayoutParams();
        layoutParams.weight = context.getResources()
                .getInteger(R.integer.alert_buttons_weight);
        layoutParams.gravity = Gravity.CENTER;
        companionDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setLayoutParams(layoutParams);
        negativeButton.setLayoutParams(layoutParams);
    }

    private LinearLayout.LayoutParams getWindowLayoutParams() {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.leftMargin = context.getResources()
                .getInteger(R.integer.alert_left_margin);
        return layoutParams;
    }

    private void fillNote(LinearLayout subMainView,
                          LinearLayout.LayoutParams layoutParams, InfoAccount account) {
        ImageView image = (ImageView) layoutInflater
                .inflate(R.layout.simple_image_view, null);
        TextView name = (TextView) layoutInflater
                .inflate(R.layout.simple_text_view, null);
        TextView age = (TextView) layoutInflater
                .inflate(R.layout.simple_text_view, null);
        TextView hobbies = (TextView) layoutInflater
                .inflate(R.layout.simple_text_view, null);
        image.setImageBitmap(decodeImage(account));
        //image.setImageBitmap(getDefaultBitmap(context));
        name.setText(context.getString(R.string.pre_format,
                context.getString(R.string.pre_name), account.getName()));
        age.setText(context.getString(R.string.pre_format,
                context.getString(R.string.pre_age), account.getAge()));
        hobbies.setText(context.getString(R.string.pre_format,
                context.getString(R.string.pre_hobby), account.getHobby()));
        subMainView.addView(image);
        subMainView.addView(name, layoutParams);
        subMainView.addView(age, layoutParams);
        subMainView.addView(hobbies, layoutParams);
    }

    private Bitmap decodeImage(InfoAccount account){
        byte[] photo = Base64.decode(account.getPhoto(), Base64.DEFAULT);
        return BitmapFactory
                .decodeByteArray(photo, 0, photo.length);
    }
}
