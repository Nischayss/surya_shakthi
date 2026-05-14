package com.suryashakti.solar.data.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "appliances")
public class Appliance {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public String emoji;
    public float watts;

    public Appliance() {}

    @Ignore
    public Appliance(String name, String emoji, float watts) {
        this.name = name;
        this.emoji = emoji;
        this.watts = watts;
    }
}
