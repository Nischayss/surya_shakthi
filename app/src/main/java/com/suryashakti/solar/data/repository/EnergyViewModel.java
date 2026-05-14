package com.suryashakti.solar.data.repository;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.suryashakti.solar.data.model.Appliance;
import com.suryashakti.solar.data.model.EnergyLog;
import com.suryashakti.solar.data.model.SaleTransaction;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EnergyViewModel extends AndroidViewModel {

    private final EnergyRepository repository;
    private final LiveData<List<EnergyLog>> allLogs;
    private final LiveData<List<EnergyLog>> last30Days;
    private final LiveData<EnergyLog> latestLog;
    private final LiveData<List<Appliance>> allAppliances;
    private final LiveData<Float> availableGridBalance;
    private final LiveData<List<SaleTransaction>> allSales;
    private final LiveData<Float> totalEarningsFromSales;

    public EnergyViewModel(Application application) {
        super(application);
        repository = new EnergyRepository(application);
        allLogs = repository.getAllLogs();
        last30Days = repository.getLast30Days();
        latestLog = repository.getLatestLog();
        allAppliances = repository.getAllAppliances();
        availableGridBalance = repository.getAvailableGridBalance();
        allSales = repository.getAllSales();
        totalEarningsFromSales = repository.getTotalEarningsFromSales();
    }

    public void insert(EnergyLog log) {
        repository.insert(log);
    }

    public void deleteById(int id) {
        repository.deleteById(id);
    }

    public void deleteAll() {
        repository.deleteAll();
    }

    public void getLogByDate(String date, EnergyRepository.Callback<EnergyLog> callback) {
        repository.getLogByDate(date, callback);
    }

    public LiveData<List<EnergyLog>> getAllLogs() { return allLogs; }

    public LiveData<List<EnergyLog>> getLast30Days() { return last30Days; }

    public LiveData<EnergyLog> getLatestLog() { return latestLog; }

    public LiveData<Float> getTotalSavingsThisMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        String fromDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.getTime());
        return repository.getTotalSavings(fromDate);
    }
    
    public LiveData<Float> getTotalGeneratedLifetime() {
        return repository.getTotalGeneratedLifetime();
    }

    public LiveData<Float> getTotalSavingsLifetime() {
        return repository.getTotalSavingsLifetime();
    }

    public LiveData<Float> getTotalNetEnergy() {
        return availableGridBalance;
    }

    public LiveData<Float> getAvoidedCostLifetime() {
        return repository.getAvoidedCostLifetime();
    }

    public LiveData<Float> getTotalKwhSold() {
        return repository.getTotalKwhSold();
    }

    public String getTodayDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    // Appliance Operations
    public void insertAppliance(Appliance appliance) {
        repository.insertAppliance(appliance);
    }

    public LiveData<List<Appliance>> getAllAppliances() {
        return allAppliances;
    }

    public void deleteAppliance(Appliance appliance) {
        repository.deleteAppliance(appliance);
    }

    // Sale Transaction Operations
    public void insertSale(SaleTransaction sale) {
        repository.insertSale(sale);
    }

    public LiveData<List<SaleTransaction>> getAllSales() {
        return allSales;
    }

    public LiveData<Float> getTotalEarningsFromSales() {
        return totalEarningsFromSales;
    }

    public void deleteAllSales() {
        repository.deleteAllSales();
    }
}
