package com.example.sae401;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.database.SQLException;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.database.sae401.DatabaseHelper;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    DatabaseHelper mDBHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button = findViewById(R.id.button);
        Button settingsButton = findViewById(R.id.settings_button);

        button.setOnClickListener(this::startGameActivity);
        settingsButton.setOnClickListener(this::startSettingsActivity);

        mDBHelper = new DatabaseHelper(this);

        try {
            mDBHelper.createDatabase();
        } catch (IOException ioe) {
            throw new Error("Unable to create database");
        }

        mDBHelper.openDatabase();
    }

    public void startGameActivity(View view) {
        Intent intent;
        intent = new Intent(this, SelectActivity.class);
        intent.putExtra("fileName", "test");
        startActivity(intent);
    }

    public void startSettingsActivity(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
}
