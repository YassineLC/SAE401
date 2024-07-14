package com.example.sae401;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.database.sae401.DatabaseHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

public class CombatActivity extends AppCompatActivity
{
    private DatabaseHelper db;
    private ArrayList<Integer> inventoryObjects ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_combat);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        inventoryObjects = getIntent().getIntegerArrayListExtra("inventory");


        db = new DatabaseHelper(this);

        try {
            db.createDatabase();
            db.openDatabase();
            Log.d("db open","La base de données a bien été ouverte");
        } catch (IOException e) {
            e.printStackTrace();
        }

        initFight(inventoryObjects);
    }




    protected void initFight(ArrayList<Integer> inventory)  {

        String[] encounterId ={String.valueOf(getIntent().getIntExtra("encounterId",0))};

        Cursor cursor = db.query("encounters", null, "id = ? ", encounterId, null, null, null);

        Encounter encounterSituation = null ;

        if (cursor != null) {
            if (cursor.moveToFirst()) {

                encounterSituation = new Encounter(cursor);
                cursor.close();
            }
        }


        Character mob;
        Character player = null;

        String[] mobId = {String.valueOf(encounterSituation.getMobId())};
        cursor = db.query("character", null, "id = ? ", mobId, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {

                mob = new Character(cursor);
                cursor.close();
            } else {
                mob = null;
            }
        } else {
            mob = null;
        }

        // ajouter la valeur
    /*    String[] playerId = {};
        cursor = db.query("character",null,"id = ? ",playerId,null,null,null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {

                player = new Character(cursor);
                cursor.close();
            }
        }


     */
        Log.d("encounter",encounterSituation.toString());
        Log.d("mob",mob.toString());


        int bgResourceId = getResources().getIdentifier(encounterSituation.getBackgroundImage(), "drawable", getPackageName());
        // image du background
        ImageView backgroundGif = findViewById(R.id.backgroundGif);
        Glide.with(this)
                .load(bgResourceId)
                .into(backgroundGif);


        // images du combat
        ImageView mobImage = findViewById(R.id.mobImage);
        ImageView playerImage = findViewById(R.id.playerImage);

        // ajouter l'image du mob
        @SuppressLint("DiscouragedApi") int mobImageId = getResources().getIdentifier(mob.getImageName(), "drawable", getPackageName());
        mobImage.setImageResource(mobImageId);

        // ajouter l'image du joueur
        //@SuppressLint("DiscouragedApi") int playerImageId = getResources().getIdentifier(player.getImageName(), "drawable", getPackageName());
        //playerImage.setImageResource(playerImageId);

        // Noms
        TextView mobName = findViewById(R.id.mobName);
        TextView playerName = findViewById(R.id.playerName);
        mobName.setText(mob.getCharName());
        //playerName.setText(player.getCharName());



        // ProgressBar
        ProgressBar mobHealth = findViewById(R.id.mobHealth);
        ProgressBar playerHealth = findViewById(R.id.playerhealth);




        Boolean yourTurn = true ;
        LinearLayout capacitiesLayout = findViewById(R.id.capacitiesLayout);

        capacitiesLayout.removeAllViews();
        // initialisation de l'inventaire si c des soins ou armes
        for(int i = 0 ; i<inventoryObjects.size();i++) {
            String[] id = {String.valueOf(inventoryObjects.get(i))};

            cursor = db.query("items", null, "id = ? ", id, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {

                    // vérification si c arme ou soin

                    String objectType = cursor.getString(cursor.getColumnIndexOrThrow("type"));
                    if (objectType.equals("weapon") || objectType.equals("heal")) {
                        String desc = cursor.getString(cursor.getColumnIndexOrThrow("description"));
                        String iconName = cursor.getString(cursor.getColumnIndexOrThrow("icon"));

                        ImageView imageView = new ImageView(this);
                        imageView.setId(inventoryObjects.get(i));
                        imageView.setLayoutParams(new LinearLayout.LayoutParams(150, 150));
                        int resID = getResources().getIdentifier("sword_black", "drawable", getPackageName());
                        imageView.setImageResource(resID);
                        capacitiesLayout.addView(imageView);
                        imageView.setOnClickListener(view -> {
                            try {

                                int value = getValue(imageView.getId());
                                String type = getObjectType(imageView.getId());
                                int mobMaxHealth = mob.getHealth();
                                int playerMaxHealth = player.getHealth();
                                String description = getObjectDesc(imageView.getId());

                                if(yourTurn)
                                {
                                    useObject(yourTurn,mob,player,type,value,description,mobHealth,playerHealth,mobMaxHealth,playerMaxHealth);
                                }

                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        });
                    }
                }
                cursor.close();
            }

        }



    }


    protected int getValue(int id) {

        String[] value = {"value"};
        String[] idValue = {String.valueOf(id)};
        Cursor cursor = db.query("items", value, "id = ? ", idValue, null, null, null);

        int val = 0 ;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
               val= cursor.getInt(cursor.getColumnIndexOrThrow("value"));
                cursor.close();
            }
        }
        return val ;
    }



    protected void useObject(Boolean yourTurn,Character mob,Character player,String objectType,int objectValue,String objectName,ProgressBar mobHealth,ProgressBar playerHealth,int mobMaxHealth,int playerMaxHealth) throws InterruptedException {
        Log.d("attack","attaque");
        Log.d("value when click",String.valueOf(objectValue));
        Log.d("type when click",objectType);

        TextView texte = findViewById(R.id.contextText);

        yourTurn = false;

        if(objectType.equals("weapon")) {

            texte.setText("Vous attaquez avec "+objectName+" et infligez "+String.valueOf(objectValue)+" de dégâts");

            Thread.sleep(300);

            mob.setHealth(mob.getHealth()-objectValue);
            mobHealth.setProgress(refreshProgressBar(mob.getHealth(), mobMaxHealth));

            Thread.sleep(300);


        } else if (objectType.equals("heal")) {

            texte.setText("Vous soignez votre personnage avec "+objectName+" et récupérez "+String.valueOf(objectValue)+" de vie");

            player.setHealth(player.getHealth()+objectValue);
            playerHealth.setProgress(refreshProgressBar(player.getHealth(), playerMaxHealth));
            Thread.sleep(300);


        }
        texte.setText(mob.getCharName()+" vous inflige "+String.valueOf(mob.getDamage())+" de dégâts");

        player.setHealth(player.getHealth() - mob.getDamage()); // a voir si on ne change pas la logique d'attaque des mobs
        playerHealth.setProgress(refreshProgressBar(player.getHealth(), playerMaxHealth));

        Thread.sleep(300);

        yourTurn = true;


    }

    protected String getObjectType(int id) {

        String[] type = {"type"};
        String[] idValue = {String.valueOf(id)};
        Cursor cursor = db.query("items", type, "id = ? ", idValue, null, null, null);

        String objectType = null ;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                objectType= cursor.getString(cursor.getColumnIndexOrThrow("type"));
                cursor.close();
            }
        }
        return objectType ;
    }


    protected int refreshProgressBar(int currentHealth,int maxHealth)
    {
      return currentHealth * 100 / maxHealth ;
    }

    protected String getObjectDesc(int id) {

        String[] type = {"description"};
        String[] idValue = {String.valueOf(id)};
        Cursor cursor = db.query("items", type, "id = ? ", idValue, null, null, null);

        String objectType = null ;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                objectType= cursor.getString(cursor.getColumnIndexOrThrow("description"));
                cursor.close();
            }
        }
        return objectType ;
    }



}
