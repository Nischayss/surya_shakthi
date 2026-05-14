package com.suryashakti.solar.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "sale_transactions")
public class SaleTransaction {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String date;
    public float kwhSold;
    public float pricePerUnit;
    public float totalAmount;

    public SaleTransaction() {}

    public SaleTransaction(String date, float kwhSold, float pricePerUnit) {
        this.date = date;
        this.kwhSold = kwhSold;
        this.pricePerUnit = pricePerUnit;
        this.totalAmount = kwhSold * pricePerUnit;
    }
}
