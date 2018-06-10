package net.nel.il.parentassistant.account;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import net.nel.il.parentassistant.R;
import net.nel.il.parentassistant.interfaces.AdapterStateListener;

public class AccountViewHolder extends RecyclerView.ViewHolder{

    ImageView elementDeleting;

    ImageView photo;

    TextView name;

    TextView age;

    TextView hobbies;

    public AccountViewHolder(View itemView) {
        super(itemView);
        elementDeleting = (ImageView) itemView.findViewById(R.id.element_deleting);
        photo = (ImageView) itemView.findViewById(R.id.photo);
        name = (TextView) itemView.findViewById(R.id.name);
        age = (TextView) itemView.findViewById(R.id.age);
        hobbies = (TextView) itemView.findViewById(R.id.hobbies);
    }

}
