package com.example.sae401;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.database.sae401.DatabaseHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

public class CombatActivity extends AppCompatActivity
{
    private  DatabaseHelper dbHelper;
    private ArrayList<JSONObject> inventoryObjects ;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_combat);

        JSONObject encounter;
        try {
            encounter = new JSONObject(getIntent().getStringExtra("encounterInfo"));
            Log.d("encounterJson : ", encounter.toString());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        ArrayList<Integer> inventory = getIntent().getIntegerArrayListExtra("inventory");

        Log.d("inventory combat : ",inventory.toString());

        // obtenir le json des items
        Resources res = this.getResources();
        @SuppressLint("DiscouragedApi") int sourceFile = res.getIdentifier("items", "raw", this.getPackageName());
        InputStream inputStream = getResources().openRawResource(sourceFile);
        Scanner scanner = new Scanner(inputStream);
        String jsonString = scanner.useDelimiter("\\A").next();

        try {
            JSONObject items = new JSONObject(jsonString);
           JSONObject item ;
           for(int i =0 ; i<inventory.size();i++){

               item = items.getJSONObject(String.valueOf(inventory.get(i)));
               inventoryObjects.add(item);

           }
        }catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            initFight(inventoryObjects, encounter);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }


        dbHelper = new DatabaseHelper(this);

        try {
            dbHelper.createDatabase();
            dbHelper.openDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Passer null pour les colonnes pour retourner toutes les colonnes
        Cursor cursor = dbHelper.query("my_table", null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                // Parcourir les colonnes
                int columnCount = cursor.getColumnCount();
                StringBuilder rowString = new StringBuilder();
                for (int i = 0; i < columnCount; i++) {
                    String columnName = cursor.getColumnName(i);
                    String value = cursor.getString(i);
                    rowString.append(columnName).append(": ").append(value).append(" | ");
                }
                // Afficher la ligne dans les logs
                Log.d("items db", rowString.toString());
            } while (cursor.moveToNext());
        }

        cursor.close();
        dbHelper.close();
    }




    protected void initFight(ArrayList<JSONObject> inventory,JSONObject encounter) throws JSONException {

        JSONObject mobInfos = encounter.getJSONObject("opposant");

        // images du combat
        ImageView mobImage = findViewById(R.id.mobImage);
        ImageView playerImage = findViewById(R.id.playerImage);

        // ajouter l'image du mob
        @SuppressLint("DiscouragedApi") int resourceId = getResources().getIdentifier(mobInfos.getString("image"), "drawable", getPackageName());
        mobImage.setImageResource(resourceId);


        // Noms
        TextView mobName = findViewById(R.id.mobName);
        TextView playerName = findViewById(R.id.playerName);
        mobName.setText(mobInfos.getString("nom"));

        // ProgressBar
        ProgressBar mobHealth = findViewById(R.id.mobHealth);
        ProgressBar playerHealth = findViewById(R.id.playerhealth);


    }

}

