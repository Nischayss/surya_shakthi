package com.suryashakti.solar.data.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.suryashakti.solar.data.model.Appliance;
import com.suryashakti.solar.data.model.EnergyLog;
import com.suryashakti.solar.data.model.SaleTransaction;

@Database(entities = {EnergyLog.class, Appliance.class, SaleTransaction.class}, version = 6, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase INSTANCE;

    public abstract EnergyDao energyDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(
                    context.getApplicationContext(),
                    AppDatabase.class,
                    "surya_shakti_db"
            ).fallbackToDestructiveMigration().build();
        }
        return INSTANCE;
    }
}
