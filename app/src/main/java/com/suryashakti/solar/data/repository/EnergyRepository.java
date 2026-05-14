package com.suryashakti.solar.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.suryashakti.solar.data.db.AppDatabase;
import com.suryashakti.solar.data.db.EnergyDao;
import com.suryashakti.solar.data.model.Appliance;
import com.suryashakti.solar.data.model.EnergyLog;
import com.suryashakti.solar.data.model.SaleTransaction;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EnergyRepository {

    private final EnergyDao dao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public EnergyRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        dao = db.energyDao();
    }

    public void insert(EnergyLog log) {
        executor.execute(() -> dao.insert(log));
    }

    public void deleteById(int id) {
        executor.execute(() -> dao.deleteById(id));
    }

    public void deleteAll() {
        executor.execute(dao::deleteAll);
    }

    public LiveData<List<EnergyLog>> getAllLogs() {
        return dao.getAllLogs();
    }

    public LiveData<List<EnergyLog>> getLast30Days() {
        return dao.getLast30Days();
    }

    public LiveData<Float> getTotalSavings(String fromDate) {
        return dao.getTotalSavings(fromDate);
    }

    public LiveData<EnergyLog> getLatestLog() {
        return dao.getLatestLog();
    }

    public LiveData<Float> getTotalGeneratedLifetime() {
        return dao.getTotalGeneratedLifetime();
    }

    public LiveData<Float> getTotalConsumedLifetime() {
        return dao.getTotalConsumedLifetime();
    }

    public LiveData<Float> getAvoidedCostLifetime() {
        return dao.getAvoidedCostLifetime();
    }

    public LiveData<Float> getTotalSavingsLifetime() {
        return dao.getTotalSavingsLifetime();
    }

    public LiveData<Float> getAvailableGridBalance() {
        return dao.getAvailableGridBalance();
    }

    public void getLogByDate(String date, Callback<EnergyLog> callback) {
        executor.execute(() -> {
            EnergyLog log = dao.getLogByDate(date);
            callback.onResult(log);
        });
    }

    // Appliance Operations
    public void insertAppliance(Appliance appliance) {
        executor.execute(() -> dao.insertAppliance(appliance));
    }

    public LiveData<List<Appliance>> getAllAppliances() {
        return dao.getAllAppliances();
    }

    public void deleteAppliance(Appliance appliance) {
        executor.execute(() -> dao.deleteAppliance(appliance));
    }

    // Sale Transactions
    public void insertSale(SaleTransaction sale) {
        executor.execute(() -> dao.insertSale(sale));
    }

    public LiveData<List<SaleTransaction>> getAllSales() {
        return dao.getAllSales();
    }

    public LiveData<Float> getTotalEarningsFromSales() {
        return dao.getTotalEarningsFromSales();
    }

    public LiveData<Float> getTotalKwhSold() {
        return dao.getTotalKwhSold();
    }

    public void deleteSale(SaleTransaction sale) {
        executor.execute(() -> dao.deleteSale(sale));
    }

    public void deleteAllSales() {
        executor.execute(dao::deleteAllSales);
    }

    public interface Callback<T> {
        void onResult(T result);
    }
}
