package com.wardcrew.batandball;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "GamePrefs";
    private static final String KEY_AI_DIFFICULTY = "ai_difficulty";
    public static final String ACTION_UPDATE_DIFFICULTY = "com.wardcrew.batandball.UPDATE_DIFFICULTY";
    public static final String EXTRA_DIFFICULTY = "difficulty";

    private SeekBar seekBar;
    private TextView seekBarValue;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        preferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        seekBar = findViewById(R.id.seekBar);
        seekBarValue = findViewById(R.id.seekBarValue);

        // Load the current AI difficulty
        int aiDifficulty = preferences.getInt(KEY_AI_DIFFICULTY, 5);
        seekBar.setProgress(aiDifficulty);
        seekBarValue.setText(String.valueOf(aiDifficulty));

        // Update the AI difficulty as the seek bar changes
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seekBarValue.setText(String.valueOf(progress));
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt(KEY_AI_DIFFICULTY, progress);
                editor.apply();

                // Broadcast the updated difficulty
                Intent intent = new Intent(ACTION_UPDATE_DIFFICULTY);
                intent.putExtra(EXTRA_DIFFICULTY, progress);
                sendBroadcast(intent);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }
        });
    }
}
