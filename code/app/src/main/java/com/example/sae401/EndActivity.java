package com.example.sae401;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;

public class EndActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end);

        ImageView backgroundImageView = findViewById(R.id.backgroundImageView);
        Glide.with(this).load(R.drawable.ending_screen1).into(backgroundImageView);

        Button restart = findViewById(R.id.restartButton);
        Button exit = findViewById(R.id.exitButton);

        restart.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        });

        exit.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
            builder.setTitle("Confirm").setMessage(R.string.ConfirmExit);

            builder.setPositiveButton("Oui", (dialog, which) -> {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                startActivity(intent);
            });
            builder.setNegativeButton("Non", (dialog, which) -> dialog.cancel());
            builder.show();
        });
    }
}
