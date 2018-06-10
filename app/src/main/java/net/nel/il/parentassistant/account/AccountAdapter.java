package net.nel.il.parentassistant.account;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.nel.il.parentassistant.R;
import net.nel.il.parentassistant.interfaces.AdapterStateListener;

import java.util.List;

public class AccountAdapter extends RecyclerView.Adapter<AccountViewHolder>
        implements View.OnClickListener{

    private AdapterStateListener adapterStateListener;

    private List<Note> notes;

    private Context context;

    AccountAdapter(List<Note> notes,
                          AdapterStateListener adapterStateListener, Context context){
        this.notes = notes;
        this.context = context;
        this.adapterStateListener = adapterStateListener;
    }

    @Override
    public AccountViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.data_element, parent, false);
        return new AccountViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AccountViewHolder holder, int position) {
        bind(holder, notes.get(position), position);
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    private void bind(AccountViewHolder holder, Note note, int position){
        holder.elementDeleting.setImageResource(note.deletingResource);
        holder.elementDeleting.setTag(position);
        holder.elementDeleting.setOnClickListener(this);
        holder.photo.setImageBitmap(note.photo);
        holder.name.setText(context.getString(R.string.pre_format,
                context.getString(R.string.pre_name), note.name));
        holder.age.setText(context.getString(R.string.pre_format,
                context.getString(R.string.pre_age), note.age));
        holder.hobbies.setText(context.getString(R.string.pre_format,
                context.getString(R.string.pre_hobby), note.hobbies));
    }

    public void remove(int position){
        notes.remove(position);
        notifyDataSetChanged();
    }

    public void setData(List<Note> notes){
        this.notes = notes;
        notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.element_deleting:
                adapterStateListener.deleteNote((int) v.getTag());
                break;
        }
    }
}
