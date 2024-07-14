package com.example.sae401;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.database.sae401.DatabaseHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

public class GameActivity extends AppCompatActivity {
    
    private DatabaseHelper db ; 
    private JSONObject data;
    private int location = -1;
    private final ArrayList<Integer> inventory = new ArrayList<Integer>();
    private int collectable = 0;
    private MediaPlayer mediaPlayer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("APP", "CREATE");
        setContentView(R.layout.activity_game);

        // Obtenez l'extra pour la nouvelle location
        int newLocation = getIntent().getIntExtra("newLocation", -1);
        if (newLocation != -1) {
            setLocation(newLocation);
        } else {
            // Continuez avec l'initialisation normale si newLocation n'est pas passé
            String fileNameWithoutExtension = getIntent().getStringExtra("fileName");
            Resources res = this.getResources();
            @SuppressLint("DiscouragedApi") int sourceFile = res.getIdentifier(fileNameWithoutExtension, "raw", this.getPackageName());
            String worldTitle = "";
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
            String newTitle = String.format(getString(R.string.app_title_name), getString(R.string.app_name), worldTitle);
            setTitle(newTitle);
            location = startLocation;
            setLocation(location);
        }

        mediaPlayer = MediaPlayer.create(this, R.raw.sound_dark_fantasy);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        db = new DatabaseHelper(this);

        try {
            db.createDatabase();
            db.openDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            LinearLayout objectsContainer = findViewById(R.id.objects_container);

            ImageView locationImage = findViewById(R.id.locationImage);
            if (locationObject.has("image")) {
                locationImage.setVisibility(View.VISIBLE);
                @SuppressLint("DiscouragedApi") int resourceId = getResources().getIdentifier(locationObject.getString("image"), "drawable", getPackageName());
                locationImage.setImageResource(resourceId);
            } else{
                locationImage.setVisibility(View.GONE);
            }
            locationImage.invalidate();
            buttonsContainer.removeAllViews();
            if(locationObject.has("actions"))
            {
                JSONArray actions = locationObject.getJSONArray("actions");

                for (int i = 0; i < actions.length(); i++) {
                    JSONObject action = actions.getJSONObject(i);
                    String actionName = action.getString("action_name");
                    Button button = new Button(this);
                    button.setText(actionName);
                    int next = action.getInt("next");
                    button.setOnClickListener(view -> setLocation(next));
                    buttonsContainer.addView(button);
                }


            }

            if(locationObject.has("encounter"))
            {
                Button fightButton = new Button(this);
                fightButton.setText(R.string.fightButton);
                fightButton.setOnClickListener(view -> {

                    int encounterId = 0;
                    try {
                        encounterId = locationObject.getJSONObject("encounter").getInt("id");
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }

                    int playerId = getIntent().getIntExtra("character_id",0);
                    Intent gameActivityIntent = new Intent(this, CombatActivity.class);
                    gameActivityIntent.putExtra("encounterId",encounterId);
                    gameActivityIntent.putExtra("playerId",playerId);
                    gameActivityIntent.putIntegerArrayListExtra("inventory",inventory);
                    startActivity(gameActivityIntent);

                });

                buttonsContainer.addView(fightButton);

            }


            objectsContainer.removeAllViews();
            if(locationObject.has("objets"))
            {

                TextView collectableTextView = findViewById(R.id.collectableTextView);
                JSONArray objets = locationObject.getJSONArray("objets");
                for(int i = 0 ; i < objets.length();i++)
                {
                    collectable = locationObject.getInt("collectable");
                    collectableTextView.setText("Remaining : "+String.valueOf(collectable));

                    JSONObject objet = objets.getJSONObject(i);
                    String[] id = {String.valueOf(objet.getInt("id"))};
                    String[] icon = {"icon"};
                    Cursor cursor = db.query("items",icon, "id = ? ", id, null, null, null);
                    if (cursor != null) {
                        if (cursor.moveToFirst()) {
                            // Récupérer la valeur du champ "icon"
                            String iconName = cursor.getString(cursor.getColumnIndexOrThrow("icon"));
                            Log.d("icontest",iconName);
                            ImageView imageView = new ImageView(this);
                            imageView.setId(objet.getInt("id"));
                            imageView.setLayoutParams(new LinearLayout.LayoutParams(32, 32));
                            int resID = getResources().getIdentifier("sword_black", "drawable", getPackageName());
                            imageView.setImageResource(resID);

                            objectsContainer.addView(imageView);
                            imageView.setOnClickListener(view -> {
                                try {
                                    collectable = collectObject(objet.getInt("id"), collectable, objectsContainer);
                                    Log.d("id",String.valueOf(objet.getInt("id")));
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                                // Mettre à jour la valeur de collectable
                                // Vous pouvez également mettre à jour l'affichage du nombre collectable ici si nécessaire
                            });
                        }
                        cursor.close();
                    }

                    /*
                    String objectName = objet.getString("description");
                    Button button = new Button(this);
                    button.setText(objectName);
                    button.setId(objet.getInt("id"));
                    objectsContainer.addView(button);
                    button.setOnClickListener(view -> {
                        try {
                            collectable = collectObject(objet.getInt("id"), collectable, objectsContainer);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        // Mettre à jour la valeur de collectable
                        // Vous pouvez également mettre à jour l'affichage du nombre collectable ici si nécessaire
                    });
                    */
                }


            }



/*            boolean isFinal = locationObject.getBoolean("final");
            if (isFinal) {
                Button button = new Button(this);
                button.setText(getString(R.string.won_game));
                button.setOnClickListener(view -> finish());
                buttonsContainer.addView(button);
            }*/

            boolean isFinal = locationObject.getBoolean("final");
            if (isFinal) {
                Intent intent = new Intent(this, EndActivity.class);
                startActivity(intent);
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
    protected int collectObject(int idObject, int collectable, LinearLayout objectsContainer) {
        if (collectable != 0) {
            collectable -= 1;
            inventory.add(idObject);
            Log.d("ajout", inventory.toString());

            // Mettre à jour le nombre collectable affiché
            TextView collectableTextView = findViewById(R.id.collectableTextView);
            collectableTextView.setText("Remaining : " + String.valueOf(collectable));

            // Mettre à jour l'affichage des objets
            ImageView imageView = objectsContainer.findViewById(idObject);
            objectsContainer.removeView(imageView);

            // Vérifier si tous les objets collectables ont été collectés
            if (collectable == 0) {
                objectsContainer.removeAllViews();
                collectableTextView.setVisibility(View.GONE);
            }
        }

        return collectable;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        db.close();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

}