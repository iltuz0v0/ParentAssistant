package net.nel.il.parentassistant.account;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SimpleAdapter;

import net.nel.il.parentassistant.R;

import java.util.List;
import java.util.Map;

public class AccountSimpleAdapter extends SimpleAdapter
        implements View.OnClickListener {

    private AccountActivity context;

    AccountSimpleAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
        super(context, data, resource, from, to);
        this.context = (AccountActivity) context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        ImageButton deleteBtn = (ImageButton) view.findViewById(R.id.delete_element);
        deleteBtn.setTag(position);
        deleteBtn.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.delete_element:
                context.deleteNote((int) view.getTag());
                break;
        }
    }
}
