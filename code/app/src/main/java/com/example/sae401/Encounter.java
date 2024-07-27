package com.example.sae401;

import android.database.Cursor;

public class Encounter {

    private String text;
    private String backgroundImage;

    public Integer getNext() {
        return next;
    }

    public void setNext(Integer next) {
        this.next = next;
    }

    public Integer getLoose() {
        return loose;
    }

    public void setLoose(Integer loose) {
        this.loose = loose;
    }

    public Integer getMobId() {
        return mobId;
    }

    public void setMobId(Integer mobId) {
        this.mobId = mobId;
    }

    private Integer next ;
    private Integer loose ;
    private Integer mobId ;


    public Encounter(String text, String backgroundImage) {
        this.text = text;
        this.backgroundImage = backgroundImage;
    }

    public Encounter(Cursor cursor) {
        this.mobId = cursor.getInt(cursor.getColumnIndexOrThrow("mobId"));
        this.text = cursor.getString(cursor.getColumnIndexOrThrow("text"));
        this.backgroundImage = cursor.getString(cursor.getColumnIndexOrThrow("backgroundImage"));
        this.next = cursor.getInt(cursor.getColumnIndexOrThrow("gagner"));
        this.loose = cursor.getInt(cursor.getColumnIndexOrThrow("loose"));
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getBackgroundImage() {
        return backgroundImage;
    }

    public void setBackgroundImage(String backgroundImage) {
        this.backgroundImage = backgroundImage;
    }



    @Override
    public String toString() {
        return "Encounter{" +
                "text='" + text + '\'' +
                ", backgroundImage='" + backgroundImage + '\'' +
                ", next=" + next +
                ", loose=" + loose +
                ", mobId=" + mobId +
                '}';
    }
}