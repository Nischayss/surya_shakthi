package com.suryashakti.solar.data.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "energy_logs", indices = {@Index(value = "date", unique = true)})
public class EnergyLog {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String date;           // yyyy-MM-dd
    public float generatedKwh;
    public float consumedKwh;
    public float batteryLevel;    // 0–100
    public String weatherCondition;
    public float netSavings;      // Now represents (Generated - Consumed) * unitPrice
    public boolean overGeneration;
    public float unitPrice;       // Price per unit at the time of logging

    public EnergyLog() {}

    @Ignore
    public EnergyLog(String date, float generatedKwh, float consumedKwh,
                     float batteryLevel, String weatherCondition, float unitPrice) {
        this.date = date;
        this.generatedKwh = generatedKwh;
        this.consumedKwh = consumedKwh;
        this.batteryLevel = batteryLevel;
        this.weatherCondition = weatherCondition;
        this.unitPrice = unitPrice;

        float net = generatedKwh - consumedKwh;
        this.overGeneration = net > 0;
        
        // Net Financial Impact = (Produced - Consumed) * unitPrice
        this.netSavings = net * unitPrice;
    }
}
