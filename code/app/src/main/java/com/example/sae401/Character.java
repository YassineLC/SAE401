package com.example.sae401;

import android.database.Cursor;

public class Character {
    private Integer id ;
    private String imageName ;
    private String charName ;
    private Integer health;
    private Integer damage ;

    public Character(String imageName, String charName, Integer health, Integer damage) {
        this.imageName = imageName;
        this.charName = charName;
        this.health = health;
        this.damage = damage;
    }

    public Character(Cursor cursor) {
        this.id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
        this.imageName = cursor.getString(cursor.getColumnIndexOrThrow("icon"));
        this.charName = cursor.getString(cursor.getColumnIndexOrThrow("name"));
        this.health = cursor.getInt(cursor.getColumnIndexOrThrow("health"));
        this.damage = cursor.getInt(cursor.getColumnIndexOrThrow("damage"));
    }

    public String getImageName() {return this.imageName;}

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getCharName() {
        return charName;
    }

    public void setCharName(String charName) {
        this.charName = charName;
    }

    @Override
    public String toString() {
        return "Character{" +
                "id=" + id +
                ", imageName='" + imageName + '\'' +
                ", charName='" + charName + '\'' +
                ", health=" + health +
                ", damage=" + damage +
                '}';
    }

    public Integer getHealth() {
        return health;
    }

    public void setHealth(Integer health) {
        this.health = health;
    }

    public Integer getDamage() {
        return damage;
    }

    public void setDamage(Integer damage) {
        this.damage = damage;
    }

    public void takeDamage(Integer damage){ this.health -= damage ;}
}
