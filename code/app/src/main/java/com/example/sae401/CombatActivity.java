package com.example.sae401;

import static com.example.sae401.TextAnimator.animateText;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.database.sae401.DatabaseHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class CombatActivity extends AppCompatActivity {
    private DatabaseHelper db;
    private ArrayList<Integer> inventoryObjects;
    private boolean isPlayerTurn = true;

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
            Log.d("db open", "La base de données a bien été ouverte");
        } catch (IOException e) {
            e.printStackTrace();
        }

        initFight(inventoryObjects);
    }

    protected void initFight(ArrayList<Integer> inventory) {
        String[] encounterId = {String.valueOf(getIntent().getIntExtra("encounterId", 0))};

        Cursor cursor = db.query("encounters", null, "id = ? ", encounterId, null, null, null);

        Encounter encounterSituation;

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                encounterSituation = new Encounter(cursor);
                cursor.close();
            } else {
                encounterSituation = null;
            }
        } else {
            encounterSituation = null;
        }

        Character mob;
        Character player;

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

        String[] playerId = {String.valueOf(getIntent().getIntExtra("playerId", 0))};
        cursor = db.query("character", null, "id = ? ", playerId, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                player = new Character(cursor);
                cursor.close();
            } else {
                player = null;
            }
        } else {
            player = null;
        }

        Log.d("encounter", encounterSituation.toString());
        Log.d("mob", mob.toString());

        int bgResourceId = getResources().getIdentifier(encounterSituation.getBackgroundImage(), "drawable", getPackageName());
        ImageView backgroundGif = findViewById(R.id.backgroundGif);
        Glide.with(this)
                .load(bgResourceId)
                .into(backgroundGif);

        ImageView mobImage = findViewById(R.id.mobImage);
        ImageView playerImage = findViewById(R.id.playerImage);

        @SuppressLint("DiscouragedApi") int mobImageId = getResources().getIdentifier(mob.getImageName(), "drawable", getPackageName());
        mobImage.setImageResource(mobImageId);

        @SuppressLint("DiscouragedApi") int playerImageId = getResources().getIdentifier(player.getImageName(), "drawable", getPackageName());
        playerImage.setImageResource(playerImageId);

        TextView mobName = findViewById(R.id.mobName);
        TextView playerName = findViewById(R.id.playerName);
        mobName.setText(mob.getCharName());
        playerName.setText(player.getCharName());

        TextView mobHealthText = findViewById(R.id.mobHealthText);
        TextView playerHealthText = findViewById(R.id.playerHealthText);

        mobHealthText.setText("PV: " + mob.getHealth() + "/" + mob.getHealth());
        playerHealthText.setText("PV: " + player.getHealth() + "/" + player.getHealth());

        ProgressBar mobHealth = findViewById(R.id.mobHealth);
        ProgressBar playerHealth = findViewById(R.id.playerhealth);
        mobHealth.setProgress(100);
        playerHealth.setProgress(100);

        LinearLayout capacitiesLayout = findViewById(R.id.capacitiesLayout);

        capacitiesLayout.removeAllViews();
        setButtonsEnabled(true);

        for (int i = 0; i < inventoryObjects.size(); i++) {
            String[] id = {String.valueOf(inventoryObjects.get(i))};

            cursor = db.query("items", null, "id = ? ", id, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
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
                        int mobMaxHealth = mob.getHealth();
                        int playerMaxHealth = player.getHealth();
                        imageView.setOnClickListener(view -> {
                            try {
                                int value = getValue(imageView.getId());
                                String type = getObjectType(imageView.getId());
                                String description = getObjectDesc(imageView.getId());

                                if (isPlayerTurn) {
                                    useObject(encounterSituation, mobHealthText, playerHealthText, mob, player, type, value, description, mobHealth, playerHealth, mobMaxHealth, playerMaxHealth);
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

    private void setButtonsEnabled(boolean enabled) {
        LinearLayout capacitiesLayout = findViewById(R.id.capacitiesLayout);
        for (int i = 0; i < capacitiesLayout.getChildCount(); i++) {
            capacitiesLayout.getChildAt(i).setEnabled(enabled);
        }
    }

    protected int getValue(int id) {
        String[] value = {"value"};
        String[] idValue = {String.valueOf(id)};
        Cursor cursor = db.query("items", value, "id = ? ", idValue, null, null, null);

        int val = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                val = cursor.getInt(cursor.getColumnIndexOrThrow("value"));
                cursor.close();
            }
        }
        return val;
    }

    protected void useObject(Encounter encounterSituation, TextView mobHealthText, TextView playerHealthText, Character mob, Character player, String objectType, int objectValue, String objectName, ProgressBar mobHealth, ProgressBar playerHealth, int mobMaxHealth, int playerMaxHealth) throws InterruptedException {
        Log.d("attack", "attaque");
        Log.d("value when click", String.valueOf(objectValue));
        Log.d("type when click", objectType);

        TextView texte = findViewById(R.id.contextText);

        if (!isPlayerTurn) {
            return;
        }

        isPlayerTurn = false;
        setButtonsEnabled(false);

        if (objectType.equals("weapon")) {
            String displayText = "Vous attaquez avec " + objectName + " et infligez " + objectValue + " de dégâts";
            animateText(texte, displayText);
            Thread.sleep(300);
            mob.setHealth(mob.getHealth() - objectValue);
            mobHealthText.setText("PV: " + mob.getHealth() + "/" + mobMaxHealth);
            mobHealth.setProgress(refreshProgressBar(mob.getHealth(), mobMaxHealth));

            if (mob.getHealth() <= 0) {
                String victoryText = "Vous avez vaincu " + mob.getCharName();
                animateText(texte, victoryText);
                mob.setHealth(0);
                mobHealthText.setText("PV: 0/" + mobMaxHealth);
                mobHealth.setProgress(0);
                Thread.sleep(500);
                int idNext = encounterSituation.getNext();
                Intent intent = new Intent(this, GameActivity.class);
                intent.putExtra("newLocation", idNext);
                startActivity(intent);
                finish();
                return;
            }
        } else if (objectType.equals("heal")) {
            String healText = "Vous soignez votre personnage avec " + objectName + " et récupérez " + objectValue + " de vie";
            animateText(texte, healText);
            player.setHealth(player.getHealth() + objectValue);
            playerHealthText.setText("PV: " + player.getHealth() + "/" + playerMaxHealth);
            playerHealth.setProgress(refreshProgressBar(player.getHealth(), playerMaxHealth));
            Thread.sleep(300);
        }

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String mobAttackText = mob.getCharName() + " vous inflige " + mob.getDamage() + " de dégâts";
                animateText(texte, mobAttackText);
                player.setHealth(player.getHealth() - mob.getDamage());
                playerHealthText.setText("PV: " + player.getHealth() + "/" + playerMaxHealth);
                playerHealth.setProgress(refreshProgressBar(player.getHealth(), playerMaxHealth));

                if (player.getHealth() <= 0) {
                    texte.setText("Vous avez été vaincu par " + mob.getCharName());

                    player.setHealth(0);
                    playerHealthText.setText("PV: 0/" + playerMaxHealth);
                    playerHealth.setProgress(0);
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    int idNext = encounterSituation.getLoose();
                    Intent intent = new Intent(CombatActivity.this, GameActivity.class);
                    intent.putExtra("newLocation", idNext);
                    startActivity(intent);
                    finish();
                }
                String yourTurn = "C'est à votre tour d'attaquer";
                animateText(texte,yourTurn);
                isPlayerTurn = true;
                setButtonsEnabled(true);
            }
        }, 3000); // Délai de 1 seconde avant que le mob attaque
    }

    protected String getObjectType(int id) {
        String[] type = {"type"};
        String[] idValue = {String.valueOf(id)};
        Cursor cursor = db.query("items", type, "id = ? ", idValue, null, null, null);

        String objectType = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                objectType = cursor.getString(cursor.getColumnIndexOrThrow("type"));
                cursor.close();
            }
        }
        return objectType;
    }

    protected int refreshProgressBar(int currentHealth, int maxHealth) {
        return currentHealth * 100 / maxHealth;
    }

    protected String getObjectDesc(int id) {
        String[] type = {"description"};
        String[] idValue = {String.valueOf(id)};
        Cursor cursor = db.query("items", type, "id = ? ", idValue, null, null, null);

        String objectType = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                objectType = cursor.getString(cursor.getColumnIndexOrThrow("description"));
                cursor.close();
            }
        }
        return objectType;
    }
}
