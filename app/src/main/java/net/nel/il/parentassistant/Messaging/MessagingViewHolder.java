package net.nel.il.parentassistant.Messaging;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import net.nel.il.parentassistant.R;

public class MessagingViewHolder extends RecyclerView.ViewHolder{

    TextView message;

    TextView time;

    MessagingViewHolder(View itemView) {
        super(itemView);
        message = itemView.findViewById(R.id.message_text_view);
        time = itemView.findViewById(R.id.time_text_view);
    }
}
