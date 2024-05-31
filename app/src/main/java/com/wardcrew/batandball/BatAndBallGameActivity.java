package com.wardcrew.batandball;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowInsets;
import android.view.WindowInsetsController;

import androidx.appcompat.app.AppCompatActivity;

public class BatAndBallGameActivity extends AppCompatActivity {

    private GameView gameView;
    private BroadcastReceiver difficultyReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gameView = new GameView(this);
        setContentView(gameView);

        // Hide the system UI for fullscreen mode
        hideSystemUI();

        // Register the broadcast receiver
        difficultyReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (SettingsActivity.ACTION_UPDATE_DIFFICULTY.equals(intent.getAction())) {
                    int newDifficulty = intent.getIntExtra(SettingsActivity.EXTRA_DIFFICULTY, 5);
                    gameView.setAiPaddleSpeed(newDifficulty);
                }
            }
        };
        IntentFilter filter = new IntentFilter(SettingsActivity.ACTION_UPDATE_DIFFICULTY);
        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY); // Set the priority
        registerReceiver(difficultyReceiver, filter, Context.RECEIVER_EXPORTED);
    }

    private void hideSystemUI() {
        final WindowInsetsController insetsController = getWindow().getInsetsController();
        if (insetsController != null) {
            insetsController.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
            insetsController.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        gameView.resume();
    }

    @Override
    public void onStop() {
        super.onStop();
        gameView.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(difficultyReceiver);
    }

    public GameView getGameView() {
        return gameView;
    }
}
