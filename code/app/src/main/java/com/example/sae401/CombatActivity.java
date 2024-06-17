package com.example.sae401;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

public class activity_combat extends AppCompatActivity
{
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


    };


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

