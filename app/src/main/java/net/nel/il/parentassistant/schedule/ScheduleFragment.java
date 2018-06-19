package net.nel.il.parentassistant.schedule;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TimePicker;

import com.google.android.gms.maps.model.LatLng;

import net.nel.il.parentassistant.R;
import net.nel.il.parentassistant.ToastManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ScheduleFragment extends Fragment implements TabLayout.OnTabSelectedListener, View.OnClickListener, TimePickerDialog.OnTimeSetListener, View.OnFocusChangeListener, SubMapFragment.MapLocationCallback {

    private ScheduleFragmentCallback scheduleFragmentCallback;

    private LinearLayout searchContainer;

    private LinearLayout eventContainer;

    private FrameLayout mainFrame;

    private Button filter;

    private Button search;

    private Button sending;

    private static final float Z = 90.0f;

    private LinearLayout filterContainer;

    private LinearLayout additionContainer;

    private FloatingActionButton eventAddition;

    private SearchFragment searchFragment;

    private EditText fromEditText;

    private EditText toEditText;

    private Button placeChoosing;

    private EditText fromEditText2;

    private EditText toEditText2;

    private Button placeChoosing2;

    private Context context;

    private ProgressBar progressBar;

    private TabLayout tabLayout;

    private List<LatLng> points = null;

    private static final int SEARCH_BUTTON = 0;

    private static final int EVENT_BUTTON = 1;

    private int editTextState = -1;

    private static final int FROM_EDIT_TEXT = 0;

    private static final int TO_EDIT_TEXT = 1;

    private static final int FROM_EDIT_TEXT_2 = 2;

    private static final int TO_EDIT_TEXT_2 = 3;

    private static final String PROGRESS_STATE = "PROGRESS_STATE";

    private static final String FROM_TEXT_VIEW = "FROM_TEXT_VIEW";

    private static final String FROM_TEXT_VIEW_2 = "FROM_TEXT_VIEW_2";

    private static final String TO_TEXT_VIEW = "TO_TEXT_VIEW";

    private static final String TO_TEXT_VIEW_2 = "TO_TEXT_VIEW_2";

    private static final String FILTER_CONTAINER_STATE = "FILTER_CONTAINER_STATE";

    private static final String ADDITION_CONTAINER_STATE = "ADDITION_CONTAINER_STATE";

    private static final String SEARCH_CONTAINER = "SEARCH_CONTAINER";

    private static final String EVENT_CONTAINER = "EVENT_CONTAINER";

    private static final String SELECTED_TAB = "SELECTED_TAB";

    private static final String LAT_1 = "LAT_1";

    private static final String LNG_1 = "LNG_1";

    private static final String LAT_2 = "LAT_2";

    private static final String LNG_2 = "LNG_2";

    private static final int FIRST_CLICK = 0;

    private static final int SECOND_CLICK = 1;

    private int selectedTab = SEARCH_BUTTON;

    private static final int TURNED = 1;

    private static final int NOT_TURNED = 0;

    private int isTurned = NOT_TURNED;


    public interface ScheduleFragmentCallback {
        void block();

        void unblock();

        Context getAppContext();

        Location getLocation();

        void sendAccountRequest(List<LatLng> points, String from, String to);

        void sendEventRequest(List<LatLng> points, String from, String to);

        EventQueue getEventQueue();

        void setScheduleFragmentState(boolean state, ScheduleFragment scheduleFragment);
    }

    @Override
    @SuppressWarnings("all")
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        enableEditTextFields(true);
        switch (editTextState) {
            case FROM_EDIT_TEXT:
                fromEditText.setText(String.format("%d:%d", hourOfDay, minute));
                break;
            case TO_EDIT_TEXT:
                toEditText.setText(String.format("%d:%d", hourOfDay, minute));
                break;
            case FROM_EDIT_TEXT_2:
                fromEditText2.setText(String.format("%d:%d", hourOfDay, minute));
                break;
            case TO_EDIT_TEXT_2:
                toEditText2.setText(String.format("%d:%d", hourOfDay, minute));
                break;
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            showTimePickerById(v.getId());
        }
    }


    @Override
    public Location getLocation() {
        return scheduleFragmentCallback.getLocation();
    }

    @Override
    public void setLocation(List<LatLng> points) {
        this.points = new ArrayList<>(points);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(PROGRESS_STATE, progressBar.getVisibility());
        outState.putString(FROM_TEXT_VIEW, fromEditText.getText().toString());
        outState.putString(FROM_TEXT_VIEW_2, fromEditText2.getText().toString());
        outState.putString(TO_TEXT_VIEW, toEditText.getText().toString());
        outState.putString(TO_TEXT_VIEW_2, toEditText2.getText().toString());
        outState.putInt(SEARCH_CONTAINER, searchContainer.getVisibility());
        outState.putInt(EVENT_CONTAINER, eventContainer.getVisibility());
        outState.putInt(FILTER_CONTAINER_STATE, filterContainer.getVisibility());
        outState.putInt(ADDITION_CONTAINER_STATE, additionContainer.getVisibility());
        outState.putInt(SELECTED_TAB, selectedTab);
        if (points.size() > 1) {
            outState.putDouble(LAT_1, points.get(FIRST_CLICK).latitude);
            outState.putDouble(LNG_1, points.get(FIRST_CLICK).longitude);
            outState.putDouble(LAT_2, points.get(SECOND_CLICK).latitude);
            outState.putDouble(LNG_2, points.get(SECOND_CLICK).longitude);
        }
    }

    @Override
    @SuppressWarnings("all")
    public void onViewStateRestored(Bundle state) {
        super.onViewStateRestored(state);
        if (state == null) {
            return;
        }
        if (state.getInt(PROGRESS_STATE) == View.VISIBLE) {
            progressBar.setVisibility(View.VISIBLE);
            filter.setVisibility(View.INVISIBLE);
        }
        fromEditText.setText(state.getString(FROM_TEXT_VIEW, ""));
        fromEditText2.setText(state.getString(FROM_TEXT_VIEW_2, ""));
        toEditText.setText(state.getString(TO_TEXT_VIEW, ""));
        toEditText2.setText(state.getString(TO_TEXT_VIEW_2, ""));
        filterContainer.setVisibility(state.getInt(FILTER_CONTAINER_STATE, View.INVISIBLE));
        additionContainer.setVisibility(state.getInt(ADDITION_CONTAINER_STATE, View.INVISIBLE));
        searchContainer.setVisibility(state.getInt(SEARCH_CONTAINER, View.VISIBLE));
        eventContainer.setVisibility(state.getInt(EVENT_CONTAINER, View.INVISIBLE));
        if (state.getDouble(LAT_1, -1.0) != -1.0) {
            points.add(new LatLng(state.getDouble(LAT_1), state.getDouble(LNG_1)));
            points.add(new LatLng(state.getDouble(LAT_2), state.getDouble(LNG_2)));
        }
        if (state.getInt(SELECTED_TAB, SEARCH_BUTTON) == EVENT_BUTTON) {
            TabLayout.Tab tab = tabLayout.getTabAt(EVENT_BUTTON);
            tab.select();
        }
    }

    public ScheduleFragment() {

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        scheduleFragmentCallback = (ScheduleFragmentCallback) activity;
        context = scheduleFragmentCallback.getAppContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        if (savedInstanceState != null) {
            isTurned = TURNED;
        }
        final View view = inflater.inflate(R.layout.fragment_schedule, container, false);
        initialization(view);
        tabLayout = view.findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText(context.getString(R.string.search)));
        tabLayout.addTab(tabLayout.newTab().setText(context.getString(R.string.event)));
        tabLayout.addOnTabSelectedListener(this);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        scheduleFragmentCallback.setScheduleFragmentState(true, this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        scheduleFragmentCallback.block();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        scheduleFragmentCallback.unblock();
        scheduleFragmentCallback.setScheduleFragmentState(false, this);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        switch (tab.getPosition()) {
            case SEARCH_BUTTON:
                selectedTab = SEARCH_BUTTON;
                filterContainer.setVisibility(View.INVISIBLE);
                searchContainer.setVisibility(View.VISIBLE);
                eventContainer.setVisibility(View.INVISIBLE);
                clearFields();
                break;
            case EVENT_BUTTON:
                selectedTab = EVENT_BUTTON;
                additionContainer.setVisibility(View.INVISIBLE);
                eventContainer.setVisibility(View.VISIBLE);
                searchContainer.setVisibility(View.INVISIBLE);
                clearFields();
                break;
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    @Override
    public void onClick(View v) {
        showTimePickerById(v.getId());
        switch (v.getId()) {
            case R.id.filter_button:
                doFilter();
                break;
            case R.id.event_addition_button:
                doEvent();
                break;
            case R.id.choice:
                doChoice();
                break;
            case R.id.choice_2:
                doChoice();
                break;
            case R.id.search_acc:
                doSearch();
                break;
            case R.id.send_event:
                doSending();
                break;
        }
    }

    private void showTimePickerById(int id) {
        switch (id) {
            case R.id.from_edit_text:
                showTimePicker(FROM_EDIT_TEXT, context.getString(R.string.from_time_picker));
                break;
            case R.id.to_edit_text:
                showTimePicker(TO_EDIT_TEXT, context.getString(R.string.to_time_picker));
                break;
            case R.id.from_edit_text_2:
                showTimePicker(FROM_EDIT_TEXT_2, context.getString(R.string.from_time_picker));
                break;
            case R.id.to_edit_text_2:
                showTimePicker(TO_EDIT_TEXT_2, context.getString(R.string.to_time_picker));
                break;
        }
    }

    private void showTimePicker(int editText, String title) {
        if (isTurned == TURNED) {
            isTurned = NOT_TURNED;
            return;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        TimePickerDialog dialog = new TimePickerDialog(getActivity(), AlertDialog.THEME_DEVICE_DEFAULT_LIGHT, this, calendar.get(Calendar.HOUR), calendar.get(Calendar.MINUTE), true);
        dialog.setTitle(title);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                enableEditTextFields(true);
            }
        });
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                enableEditTextFields(true);
            }
        });
        enableEditTextFields(false);
        editTextState = editText;
        dialog.show();
    }

    private void enableEditTextFields(boolean enable) {
        fromEditText.setEnabled(enable);
        toEditText.setEnabled(enable);
    }

    private void doFilter() {
        if (filterContainer.getVisibility() == View.INVISIBLE) {
            filterContainer.setVisibility(View.VISIBLE);
        } else {
            filterContainer.setVisibility(View.INVISIBLE);
            clearFields();
        }
    }

    private void clearFields() {
        toEditText.setText("");
        fromEditText.setText("");
        points.clear();
    }

    private void doChoice() {
        SubMapFragment subMapFragment = new SubMapFragment();
        getFragmentManager().beginTransaction().add(R.id.main_frame, subMapFragment).addToBackStack(null).commit();
        subMapFragment.setReference(this);
    }

    private void doSearch() {
        if (progressBar.getVisibility() != View.VISIBLE) {
            if (ifFieldsFilled(fromEditText, toEditText)) {
                filterContainer.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                filter.setEnabled(false);
                scheduleFragmentCallback.sendAccountRequest(points, fromEditText.getText().toString(), toEditText.getText().toString());
            } else {
                ToastManager.showToast(context.getString(R.string.not_filled), context);
            }
        }
    }

    private void doSending() {
        if (ifFieldsFilled(fromEditText2, toEditText2)) {
            scheduleFragmentCallback.sendEventRequest(points, fromEditText2.getText().toString(), toEditText2.getText().toString());
            additionContainer.setVisibility(View.INVISIBLE);
        } else {
            ToastManager.showToast(context.getString(R.string.not_filled), context);
        }
    }

    private boolean ifFieldsFilled(EditText fromEditText, EditText toEditText) {
        return !TextUtils.isEmpty(fromEditText.getText().toString()) && !TextUtils.isEmpty(toEditText.getText().toString()) && points != null && points.size() > 0;
    }

    private void doEvent() {
        if (additionContainer.getVisibility() == View.INVISIBLE) {
            additionContainer.setVisibility(View.VISIBLE);
        } else {
            additionContainer.setVisibility(View.INVISIBLE);
            clearFields();
        }
    }

    public void setOutputAccount(boolean state) {
        progressBar.setVisibility(View.INVISIBLE);
        filter.setEnabled(true);
        searchFragment.setOutputAccount(state);
    }

    private void initialization(View view) {
        mainFrame = (FrameLayout) view.findViewById(R.id.main_frame);
        mainFrame.setZ(Z);
        additionContainer = (LinearLayout) view.findViewById(R.id.addition_container);
        search = (Button) view.findViewById(R.id.search_acc);
        search.setOnClickListener(this);
        sending = (Button) view.findViewById(R.id.send_event);
        sending.setOnClickListener(this);
        progressBar = (ProgressBar) view.findViewById(R.id.request_progress);
        fromEditText = (EditText) view.findViewById(R.id.from_edit_text);
        fromEditText.setOnFocusChangeListener(this);
        fromEditText.setOnClickListener(this);
        placeChoosing = (Button) view.findViewById(R.id.choice);
        placeChoosing.setOnClickListener(this);
        toEditText = (EditText) view.findViewById(R.id.to_edit_text);
        toEditText.setOnFocusChangeListener(this);
        toEditText.setOnClickListener(this);
        fromEditText2 = (EditText) view.findViewById(R.id.from_edit_text_2);
        fromEditText2.setOnFocusChangeListener(this);
        fromEditText2.setOnClickListener(this);
        placeChoosing2 = (Button) view.findViewById(R.id.choice_2);
        placeChoosing2.setOnClickListener(this);
        toEditText2 = (EditText) view.findViewById(R.id.to_edit_text_2);
        toEditText2.setOnFocusChangeListener(this);
        toEditText2.setOnClickListener(this);
        filter = (Button) view.findViewById(R.id.filter_button);
        filter.setOnClickListener(this);
        points = new ArrayList<>();
        eventAddition = (FloatingActionButton) view.findViewById(R.id.event_addition_button);
        searchContainer = (LinearLayout) view.findViewById(R.id.search_container);
        eventContainer = (LinearLayout) view.findViewById(R.id.events_container);
        eventAddition.setOnClickListener(this);
        filterContainer = (LinearLayout) view.findViewById(R.id.filter_container);
        searchFragment = (SearchFragment) getChildFragmentManager().findFragmentById(R.id.search_fragment);
    }

}
