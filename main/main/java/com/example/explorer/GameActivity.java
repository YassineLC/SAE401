package com.example.explorer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.Scanner;

public class GameActivity extends AppCompatActivity {
    private JSONObject data;
    private int location = -1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("APP","CREATE");
        setContentView(R.layout.activity_game);
        String fileNameWithoutExtension = getIntent().getStringExtra("fileName");
        Resources res = this.getResources();
        @SuppressLint("DiscouragedApi") int sourceFile = res.getIdentifier(fileNameWithoutExtension, "raw", this.getPackageName());
        String worldTitle="";
        int startLocation = 0;
        InputStream inputStream = getResources().openRawResource(sourceFile);
        Scanner scanner = new Scanner(inputStream);
        String jsonString = scanner.useDelimiter("\\A").next();
        try {
            data = new JSONObject(jsonString);
            worldTitle = data.getString("title");
            startLocation = data.getInt("start");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String newTitle = String.format(getString(R.string.app_title_name),getString(R.string.app_name),worldTitle);
        setTitle(newTitle);
        location = startLocation;
        setLocation(location);
    }
    protected void setLocation(int newLoc) {
        location = newLoc;
        try {
            JSONObject locationObject = data.getJSONArray("places").getJSONObject(location);
            TextView locationTitleTextView = findViewById(R.id.locationName);
            locationTitleTextView.setText(locationObject.getString("name"));
            TextView locationDescTextView = findViewById(R.id.locationDesc);
            locationDescTextView.setText(locationObject.getString("desc"));
            LinearLayout buttonsContainer = findViewById(R.id.buttons_container);
            ImageView locationImage = findViewById(R.id.locationImage);
            if (locationObject.has("image")) {
                locationImage.setVisibility(View.VISIBLE);
                @SuppressLint("DiscouragedApi") int resourceId = getResources().getIdentifier(locationObject.getString("image"), "drawable", getPackageName());
                locationImage.setImageResource(resourceId);
            } else{
                locationImage.setVisibility(View.GONE);
            }
            locationImage.invalidate();
            JSONArray actions = locationObject.getJSONArray("actions");
            buttonsContainer.removeAllViews();
            for (int i = 0; i < actions.length(); i++) {
                JSONObject action = actions.getJSONObject(i);
                String actionName = action.getString("action_name");
                Button button = new Button(this);
                button.setText(actionName);
                int next = action.getInt("next");
                button.setOnClickListener(view -> setLocation(next));
                buttonsContainer.addView(button);
            }
            boolean isFinal = locationObject.getBoolean("final");
            if (isFinal) {
                Button button = new Button(this);
                button.setText(getString(R.string.won_game));
                button.setOnClickListener(view -> finish());
                buttonsContainer.addView(button);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        // put string value
        Log.d("APP","SAVE");
        outState.putInt("location",location);
        super.onSaveInstanceState(outState);
    }
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        // Remarque : on peut aussi restaurer l'état dans onCreate()
        // Cela évite un double appel à setLocation()
        // Ce serait mieux mais moins pédagogique
        super.onRestoreInstanceState(savedInstanceState);
        Log.d("APP","RESTORE");
        int reloc=savedInstanceState.getInt("location");
        setLocation(reloc);
    }
}