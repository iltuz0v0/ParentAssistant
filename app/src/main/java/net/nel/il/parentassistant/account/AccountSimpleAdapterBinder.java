package net.nel.il.parentassistant.account;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import net.nel.il.parentassistant.R;

public class AccountSimpleAdapterBinder implements SimpleAdapter.ViewBinder {

    private Context context;

    AccountSimpleAdapterBinder(Context context) {
        this.context = context;
    }

    @Override
    public boolean setViewValue(View view, Object o, String s) {
        switch (view.getId()) {
            case R.id.photo:
                ((ImageView) view).setImageBitmap((Bitmap) o);
                break;
            case R.id.name:
                ((TextView) view).setText(
                        context.getString(R.string.pre_format,
                                context.getString(R.string.pre_name), (String) o));
                break;
            case R.id.age:
                ((TextView) view).setText(
                        context.getString(R.string.pre_format,
                                context.getString(R.string.pre_age), (String) o));
                break;
            case R.id.hobbies:
                ((TextView) view).setText(
                        context.getString(R.string.pre_format,
                                context.getString(R.string.pre_hobby), (String) o));
                break;
            case R.id.delete_element:
                ((ImageButton) view).setImageResource(R.drawable.delete_element);
                break;
        }

        return true;
    }
}
