package net.nel.il.parentassistant.main;

import android.app.Activity;
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

    private static final String DEFAULT_SERVER_VALUE = "null";

    public static final int SHOW_INFO_WINDOW = 18;

    @SuppressWarnings("all")
    public void createCompanionRequestWindow(Activity context,
                                             Object accounts,
                                             final int companionId, final int isCompanion,
                                             final MajorHandler majorHandler) {
        List<InfoAccount> accountsCopy = (List<InfoAccount>) accounts;
        View view = createView(context, accountsCopy);
        AlertDialog.Builder companionDialogBuilder = new AlertDialog.Builder(context,
                R.style.InfoWindowAlertDialog);
        companionDialogBuilder.setTitle(R.string.alert_title);
        companionDialogBuilder.setView(view);
        companionDialogBuilder.setPositiveButton(R.string.alert_request,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (isCompanion == 0) {
                            companionRequest(companionId, majorHandler);
                        }
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
        fixButtonPositions(context, companionDialog);
    }

    private View createView(Context context, List<InfoAccount> accounts) {
        View view = LayoutInflater.from(context).inflate(R.layout.marker_info, null);
        ScrollView mainView = (ScrollView) view;
        LinearLayout.LayoutParams layoutParams = getWindowLayoutParams(context);
        LinearLayout subMainView = (LinearLayout) mainView.getChildAt(0);
        subMainView.setId(R.id.info_scroll_linear);
        for (InfoAccount account : accounts) {
            fillNote(context, subMainView, layoutParams, account);
        }
        return view;
    }

    private void companionRequest(int companionId, MajorHandler majorHandler) {
        majorHandler.doCompanionRequest(MapManager.COMPANION_REQUEST, companionId);
    }

    private void fixButtonPositions(Context context, AlertDialog companionDialog) {
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

    private LinearLayout.LayoutParams getWindowLayoutParams(Context context) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.leftMargin = context.getResources()
                .getInteger(R.integer.alert_left_margin);
        return layoutParams;
    }

    private void fillNote(Context context, LinearLayout subMainView,
                          LinearLayout.LayoutParams layoutParams,
                          InfoAccount account) {
        ImageView image = (ImageView) LayoutInflater.from(context)
                .inflate(R.layout.simple_image_view, null);
        TextView name = (TextView) LayoutInflater.from(context)
                .inflate(R.layout.simple_text_view, null);
        TextView age = (TextView) LayoutInflater.from(context)
                .inflate(R.layout.simple_text_view, null);
        TextView hobbies = (TextView) LayoutInflater.from(context)
                .inflate(R.layout.simple_text_view, null);
        if (!account.getName().equals(DEFAULT_SERVER_VALUE)) {
            fillValues(context, name, age, hobbies, account);
            fillImage(image, account);
        } else {
            fillDefaultValues(context, name, age, hobbies);
            fillDefaultImage(context, image);
        }
        subMainView.addView(image);
        subMainView.addView(name, layoutParams);
        subMainView.addView(age, layoutParams);
        subMainView.addView(hobbies, layoutParams);
    }

    private void fillValues(Context context, TextView name, TextView age,
                            TextView hobbies, InfoAccount account) {
        name.setText(context.getString(R.string.pre_format,
                context.getString(R.string.pre_name), account.getName()));
        age.setText(context.getString(R.string.pre_format,
                context.getString(R.string.pre_age), account.getAge()));
        hobbies.setText(context.getString(R.string.pre_format,
                context.getString(R.string.pre_hobby), account.getHobby()));
    }

    private void fillDefaultValues(Context context, TextView name, TextView age,
                                   TextView hobbies) {
        name.setText(context.getString(R.string.pre_format,
                context.getString(R.string.pre_name), ""));
        age.setText(context.getString(R.string.pre_format,
                context.getString(R.string.pre_age), ""));
        hobbies.setText(context.getString(R.string.pre_format,
                context.getString(R.string.pre_hobby), ""));
    }

    private void fillImage(ImageView image, InfoAccount account) {
        image.setImageBitmap(decodeImage(account));
    }

    private void fillDefaultImage(Context context, ImageView image) {
        image.setImageBitmap(getDefaultBitmap(context));
    }

    private Bitmap decodeImage(InfoAccount account) {
        byte[] photo = Base64.decode(account.getPhoto(), Base64.DEFAULT);
        return BitmapFactory
                .decodeByteArray(photo, 0, photo.length);
    }

    private Bitmap getDefaultBitmap(Context context) {
        Drawable drawable = ContextCompat.getDrawable(context, R.drawable.default_face);
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }





    @SuppressWarnings("all")
    public void createCompanionRequestWindow(Activity context,
                                             Object accounts) {
        List<InfoAccount> accountsCopy = (List<InfoAccount>) accounts;
        View view = createView(context, accountsCopy);
        AlertDialog.Builder companionDialogBuilder = new AlertDialog.Builder(context,
                R.style.InfoWindowAlertDialog);
        companionDialogBuilder.setTitle(R.string.alert_title);
        companionDialogBuilder.setView(view);
        companionDialogBuilder.setNegativeButton(R.string.alert_cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
        AlertDialog companionDialog = companionDialogBuilder.create();
        companionDialog.show();
        fixButtonPositions(context, companionDialog);
    }
}
