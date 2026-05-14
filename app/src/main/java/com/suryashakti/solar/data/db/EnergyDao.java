package com.suryashakti.solar.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.suryashakti.solar.data.model.Appliance;
import com.suryashakti.solar.data.model.EnergyLog;

import java.util.List;

@Dao
public interface EnergyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(EnergyLog log);

    @Query("SELECT * FROM energy_logs ORDER BY date DESC")
    LiveData<List<EnergyLog>> getAllLogs();

    @Query("SELECT * FROM energy_logs ORDER BY date DESC LIMIT 30")
    LiveData<List<EnergyLog>> getLast30Days();

    @Query("SELECT * FROM energy_logs WHERE date = :date LIMIT 1")
    EnergyLog getLogByDate(String date);

    @Query("SELECT SUM(netSavings) FROM energy_logs WHERE date >= :fromDate")
    LiveData<Float> getTotalSavings(String fromDate);

    @Query("SELECT * FROM energy_logs ORDER BY date DESC LIMIT 1")
    LiveData<EnergyLog> getLatestLog();

    @Query("DELETE FROM energy_logs")
    void deleteAll();

    @Query("DELETE FROM energy_logs WHERE id = :id")
    void deleteById(int id);

    // Advanced: Lifetime Stats
    @Query("SELECT SUM(generatedKwh) FROM energy_logs")
    LiveData<Float> getTotalGeneratedLifetime();

    @Query("SELECT SUM(netSavings) FROM energy_logs")
    LiveData<Float> getTotalSavingsLifetime();

    @Query("SELECT SUM(generatedKwh - consumedKwh) FROM energy_logs")
    LiveData<Float> getTotalNetEnergy();

    // Appliance Operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAppliance(Appliance appliance);

    @Query("SELECT * FROM appliances ORDER BY name ASC")
    LiveData<List<Appliance>> getAllAppliances();

    @Delete
    void deleteAppliance(Appliance appliance);
}
