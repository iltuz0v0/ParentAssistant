package net.nel.il.parentassistant.Messaging;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.nel.il.parentassistant.R;
import net.nel.il.parentassistant.model.Message;

import java.util.ArrayList;
import java.util.List;

public class MessagingAdapter extends RecyclerView.Adapter<MessagingViewHolder> {

    private List<Message> messages;

    private Context context;

    MessagingAdapter(List<Message> messages, Context context) {
        this.messages = new ArrayList<>(messages);
        this.context = context;
    }

    @Override
    public MessagingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message, parent, false);
        return new MessagingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MessagingViewHolder holder, int position) {
        System.out.println("position " + position + " " + messages.get(position).isOwner());
        innerBind(holder, messages.get(position));
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    private void innerBind(MessagingViewHolder holder, Message message) {
        System.out.println("Bind");
        if (message.isOwner()) {
            fillView(holder, message);
            holder.message.setBackground(context.getDrawable(R.drawable.custom_text_view));
        } else {
            fillView(holder, message);
            holder.message.setGravity(Gravity.END);
            holder.message.setBackground(context.getDrawable(R.drawable.custom_text_view_outer));
        }
    }

    public void addMessage(Message message) {
        System.out.println("add position " + messages.size() + " " + message.isOwner());
        messages.add(message);
        notifyDataSetChanged();
    }

    private void fillView(MessagingViewHolder holder, Message message) {
        holder.message.setText(message.getMessage());
        holder.time.setText(DateFormat.format(context.getString(R.string.time), message.getTime().getTime()));
        if (message.isState()) {
            holder.state.setText(context.getString(R.string.bad_message_state));
        }
    }

    public void setBadMessage(int position) {
        messages.get(position).setBadState(true);
        notifyDataSetChanged();
    }

}
