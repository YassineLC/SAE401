package com.example.sae401;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.database.sae401.DatabaseHelper;

public class SelectActivity extends AppCompatActivity {

    private LinearLayout character1Layout, character2Layout, character3Layout;
    private TextView character1Name, character1Pv, character1Damage, character2Name, character2Pv, character2Damage, character3Name, character3Pv, character3Damage;
    private ImageView character1Image, character2Image, character3Image;
    private LinearLayout selectedCharacterLayout;
    private Button confirmButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);

        character1Layout = findViewById(R.id.character1_layout);
        character1Name = findViewById(R.id.character1_name);
        character1Pv = findViewById(R.id.character1_pv);
        character1Damage = findViewById(R.id.character1_damage);
        character1Image = findViewById(R.id.character1_image);

        character2Layout = findViewById(R.id.character2_layout);
        character2Name = findViewById(R.id.character2_name);
        character2Pv = findViewById(R.id.character2_pv);
        character2Damage = findViewById(R.id.character2_damage);
        character2Image = findViewById(R.id.character2_image);

        character3Layout = findViewById(R.id.character3_layout);
        character3Name = findViewById(R.id.character3_name);
        character3Pv = findViewById(R.id.character3_pv);
        character3Damage = findViewById(R.id.character3_damage);
        character3Image = findViewById(R.id.character3_image);

        confirmButton = findViewById(R.id.confirm_button);

        loadCharactersFromDatabase();

        character1Layout.setOnClickListener(view -> selectCharacter(character1Layout));
        character2Layout.setOnClickListener(view -> selectCharacter(character2Layout));
        character3Layout.setOnClickListener(view -> selectCharacter(character3Layout));

        confirmButton.setOnClickListener(view -> confirmSelection());
    }

    private void selectCharacter(LinearLayout selectedLayout) {
        if (selectedCharacterLayout != null) {
            selectedCharacterLayout.setBackgroundColor(Color.TRANSPARENT); // Réinitialiser la couleur de fond du layout précédent
        }
        selectedCharacterLayout = selectedLayout;
        selectedCharacterLayout.setBackgroundColor(Color.LTGRAY); // Surligner le layout sélectionné
    }

    private void confirmSelection() {
        if (selectedCharacterLayout == null) {
            return; // Aucun personnage sélectionné
        }

        int selectedId = -1;
        String selectedName = null;
        int selectedHealth = 0;
        int selectedDamage = 0;
        String selectedIcon = null;

        Cursor cursor = getCharacterCursor();

        if (cursor != null) {
            while (cursor.moveToNext()) {
                @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex("id"));
                if ((selectedCharacterLayout == character1Layout && id == 1) ||
                        (selectedCharacterLayout == character2Layout && id == 2) ||
                        (selectedCharacterLayout == character3Layout && id == 3)) {

                    selectedId = id;
                    @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex("name")).trim();
                    @SuppressLint("Range") int health = cursor.getInt(cursor.getColumnIndex("health"));
                    @SuppressLint("Range") int damage = cursor.getInt(cursor.getColumnIndex("damage"));
                    @SuppressLint("Range") String icon = cursor.getString(cursor.getColumnIndex("icon")).trim();

                    selectedName = name;
                    selectedHealth = health;
                    selectedDamage = damage;
                    selectedIcon = icon;

                    break;
                }
            }
            cursor.close();
        }

        if (selectedId != -1) {
            int finalSelectedId = selectedId;
            new AlertDialog.Builder(this)
                    .setTitle(R.string.dialog_title)
                    .setMessage(R.string.dialog_message)
                    .setPositiveButton(R.string.yes, (dialog, which) -> {
                        Intent intent = new Intent(SelectActivity.this, GameActivity.class);
                        intent.putExtra("fileName", "test");
                        intent.putExtra("character_id", finalSelectedId);
                        startActivity(intent);
                    })
                    .setNegativeButton(R.string.no, null)
                    .show();
        }
    }

    private Cursor getCharacterCursor() {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return db.query("character", null, null, null, null, null, null);
    }


    private void loadCharactersFromDatabase() {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query("character", null, null, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int count = 0;
            do {
                @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex("name")).trim();
                @SuppressLint("Range") int health = cursor.getInt(cursor.getColumnIndex("health"));
                @SuppressLint("Range") int damage = cursor.getInt(cursor.getColumnIndex("damage"));
                @SuppressLint("Range") String icon = cursor.getString(cursor.getColumnIndex("icon")).trim();

                String imageUri = "android.resource://" + getPackageName() + "/drawable/" + icon;
                Log.d("LoadCharacter", "Name: " + name + ", Icon: " + icon + ", Uri: " + imageUri);

                switch (count) {
                    case 0:
                        character1Name.setText(name);
                        character1Pv.append(String.valueOf(health));
                        character1Damage.append(String.valueOf(damage));
                        Glide.with(this).load(imageUri).into(character1Image);
                        break;
                    case 1:
                        character2Name.setText(name);
                        character2Pv.append(String.valueOf(health));
                        character2Damage.append(String.valueOf(damage));
                        Glide.with(this).load(imageUri).into(character2Image);
                        break;
                    case 2:
                        character3Name.setText(name);
                        character3Pv.append(String.valueOf(health));
                        character3Damage.append(String.valueOf(damage));
                        Glide.with(this).load(imageUri).into(character3Image);
                        break;
                }
                count++;
            } while (cursor.moveToNext());

            cursor.close();
        }
    }
}
