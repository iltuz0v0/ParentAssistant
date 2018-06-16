package net.nel.il.parentassistant.schedule;


import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.nel.il.parentassistant.R;
import net.nel.il.parentassistant.model.OutputAccount;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment implements
        SearchingAdapter.SearchingAdapterCallback{

    private RecyclerView recyclerView;

    private SearchingAdapter searchingAdapter;

    private EventQueue eventQueue;

    private ScheduleFragment.ScheduleFragmentCallback scheduleFragmentCallback;


    public SearchFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        scheduleFragmentCallback = (ScheduleFragment.ScheduleFragmentCallback) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        recyclerView = view.findViewById(R.id.search_recycler_view);
        eventQueue = scheduleFragmentCallback.getEventQueue();
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        searchingAdapter = new SearchingAdapter(eventQueue.getOutputObject(),
                this);
        recyclerView.setAdapter(searchingAdapter);
        return view;
    }

    public void setOutputAccount(boolean state){
        searchingAdapter.setData(eventQueue.getOutputObject());
    }

    @Override
    public Context getAppContext() {
        return scheduleFragmentCallback.getAppContext();
    }

    @Override
    public Activity getContext() {
        return getActivity();
    }
}
