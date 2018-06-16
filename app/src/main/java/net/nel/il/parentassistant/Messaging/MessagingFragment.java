package net.nel.il.parentassistant.Messaging;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import net.nel.il.parentassistant.R;
import net.nel.il.parentassistant.model.Message;

import java.util.ArrayList;
import java.util.List;

public class MessagingFragment extends Fragment implements View.OnClickListener{

    private MessagingDialogCallback messagingDialogFragment;

    private EditText messagingEditText;

    private Button messagingButton;

    private RecyclerView recyclerView;

    private MessagingAdapter messagesAdapter;

    private Messaging messaging;

    private List<Message> messages;

    public interface MessagingDialogCallback{
        void sendMessage(String message, int position, int finishPosition);
        Messaging getMessagingObject();
        void setMessagingDialogState(boolean state, MessagingFragment messagingFragment);
        void block();
        void unblock();
        void setDefaultMessagingIcon();
        void refreshChatState(MessagingFragment messagingFragment);
        Context getAppContext();
    }

    public MessagingFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        messagingDialogFragment = (MessagingDialogCallback) activity;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        messagingDialogFragment.refreshChatState(this);
        messaging = messagingDialogFragment.getMessagingObject();
        messagingDialogFragment.setDefaultMessagingIcon();
        messages = new ArrayList<>();
        messages.addAll(messaging.getMessages());
        messagesAdapter = new MessagingAdapter(messages,
                messagingDialogFragment.getAppContext());
        recyclerView.setAdapter(messagesAdapter);
        messagingDialogFragment.setMessagingDialogState(true, this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        messagingDialogFragment.setMessagingDialogState(false, this);
        messagingDialogFragment.unblock();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        messagingDialogFragment.block();
        inflater.inflate(R.menu.fragment_menu, menu);
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View view =  inflater.inflate(R.layout.fragment_messaging_dialog,
                container, false);
        messagingEditText = (EditText) view.findViewById(R.id.messaging_edit_text);
        messagingButton = (Button) view.findViewById(R.id.messaging_button);
        messagingButton.setOnClickListener(this);
        recyclerView = (RecyclerView) view.findViewById(R.id.messaging_recycle_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(messagingDialogFragment.getAppContext()));
        return view;
    }

    public void refreshChat(){
        List<String> outerMessages = messaging.getOuterMessages();
        for(String message : outerMessages){
            messages.add(new Message(message, false, System.currentTimeMillis()));
        }
        messagesAdapter.addMessage(messages.get(messages.size()-1));
    }

    public void setBadMessage(int position, int finishPosition){
        messagesAdapter.setBadMessage(position);
        messaging.setBadMessage(finishPosition);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.messaging_button:
                if(!TextUtils.isEmpty(messagingEditText.getText().toString())) {
                    int position;
                    messages.add(new Message(messagingEditText.getText().toString(),
                            true, System.currentTimeMillis()));
                    position = messaging.addInnerMessage(messagingEditText.getText().toString());
                    messagingDialogFragment.sendMessage(
                            messagingEditText.getText().toString(),
                            messages.size()-1, position);
                    messagesAdapter.addMessage(messages.get(messages.size()-1));
                    messagingEditText.setText("");
                }
                break;
        }
    }



}
