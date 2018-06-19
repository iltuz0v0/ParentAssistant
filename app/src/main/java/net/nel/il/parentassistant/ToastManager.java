package net.nel.il.parentassistant;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class ToastManager {

    public static void showToast(String message, Context context) {
        Toast toast = new Toast(context);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        View view = LayoutInflater.from(context).inflate(R.layout.toast, null);
        ((TextView) view.findViewById(R.id.toast_text_view)).setText(message);
        toast.setView(view);
        toast.show();
    }
}
