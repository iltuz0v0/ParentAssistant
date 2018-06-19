package net.nel.il.parentassistant.schedule;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import net.nel.il.parentassistant.R;

public class SearchingViewHolder extends RecyclerView.ViewHolder {

    TextView fromTextView;

    TextView toTextView;

    Button place;

    Button account;

    Button requestSending;

    public SearchingViewHolder(View itemView) {
        super(itemView);
        fromTextView = itemView.findViewById(R.id.from_dynamic);
        toTextView = itemView.findViewById(R.id.to_dynamic);
        place = (Button) itemView.findViewById(R.id.look_in_place);
        account = (Button) itemView.findViewById(R.id.look_in_account);
        requestSending = (Button) itemView.findViewById(R.id.send_request);
    }
}
