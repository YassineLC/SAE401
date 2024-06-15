package com.example.sae401;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button = findViewById(R.id.button);
        Button settingsButton = findViewById(R.id.settings_button);

        button.setOnClickListener(this::startGameActivity);
        settingsButton.setOnClickListener(this::startSettingsActivity);
    }

    public void startGameActivity(View view) {
        Intent intent;
        intent = new Intent(this, GameActivity.class);
        intent.putExtra("fileName", "test");
        startActivity(intent);
    }

    public void startSettingsActivity(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
}
