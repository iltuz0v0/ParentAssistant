package net.nel.il.parentassistant.settings;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import net.nel.il.parentassistant.R;
import net.nel.il.parentassistant.main.MapManager;

public class SettingsActivity extends AppCompatActivity
        implements SeekBar.OnSeekBarChangeListener, View.OnClickListener {

    private SeekBar radiusSeekBar;

    private TextView radiusSeekBarProgress;

    private Button radiusConfirmation;

    private SharedPreferenceManager sharedPreferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        variablesInitialization();
        seekBarInitialization();
    }

    @Override
    @SuppressWarnings("all")
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        radiusSeekBarProgress.setText(Integer.toString(i));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @SuppressWarnings("all")
    private void seekBarInitialization() {
        float radius = sharedPreferenceManager.getValue(this,
                R.string.settings_file, R.string.setting_radius);
        if (radius == -1.0f) {
            radius = MapManager.defaultCircleRadius;
        }
        radiusSeekBarProgress.setText(Float.toString(radius));
        radiusSeekBar.setProgress((int) radius);
    }

    @Override
    public void onClick(View view) {
        sharedPreferenceManager.setValue(this, R.string.settings_file,
                (float) radiusSeekBar.getProgress(), R.string.setting_radius);
    }

    private void variablesInitialization() {
        sharedPreferenceManager = new SharedPreferenceManager();
        radiusSeekBar = (SeekBar) findViewById(R.id.radiusSeekBar);
        radiusSeekBarProgress = (TextView) findViewById(R.id.radiusSeekBarProgress);
        radiusConfirmation = (Button) findViewById(R.id.radiusConfirmation);
        radiusSeekBar.setOnSeekBarChangeListener(this);
        radiusConfirmation.setOnClickListener(this);
    }
}
