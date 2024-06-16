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
        // If the method has signature (View v), one can use a direct method reference
        button.setOnClickListener(this::startGameActivity);
    }
    public void startGameActivity(View view) {
        Intent intent;
        intent = new Intent(this, GameActivity.class);
        intent.putExtra("fileName", "test");
        startActivity(intent);
    }
}