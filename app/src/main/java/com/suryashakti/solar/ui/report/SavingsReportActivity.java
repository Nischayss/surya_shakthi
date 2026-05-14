package com.suryashakti.solar.ui.report;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.suryashakti.solar.R;
import com.suryashakti.solar.data.model.EnergyLog;
import com.suryashakti.solar.data.repository.EnergyViewModel;
import com.suryashakti.solar.databinding.ActivitySavingsReportBinding;
import com.suryashakti.solar.utils.ThemeManager;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class SavingsReportActivity extends AppCompatActivity {

    private ActivitySavingsReportBinding binding;
    private EnergyViewModel viewModel;
    private List<EnergyLog> currentLogs = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        binding = ActivitySavingsReportBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("30-Day Savings Report");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        viewModel = new ViewModelProvider(this).get(EnergyViewModel.class);

        viewModel.getLast30Days().observe(this, logs -> {
            this.currentLogs = logs;
            updateReport(logs);
        });

        viewModel.getTotalSavingsThisMonth().observe(this, total -> {
            binding.tvTotalSavings.setText(total != null ? String.format("₹%.2f", total) : "₹0.00");
        });

        // Advanced Feature: Export CSV
        binding.btnExportCsv.setOnClickListener(v -> exportLogsToCSV());
    }

    private void updateReport(List<EnergyLog> logs) {
        if (logs == null || logs.isEmpty()) return;

        float totalGen = 0, totalCons = 0, totalSavings = 0;
        int overGenDays = 0;
        for (EnergyLog log : logs) {
            totalGen += log.generatedKwh;
            totalCons += log.consumedKwh;
            totalSavings += log.netSavings;
            if (log.overGeneration) overGenDays++;
        }

        binding.tvTotalGenerated.setText(String.format("%.1f kWh", totalGen));
        binding.tvTotalConsumed.setText(String.format("%.1f kWh", totalCons));
        binding.tvOverGenDays.setText(overGenDays + " days");
        binding.tvTotalSavings.setText(String.format("₹%.2f", totalSavings));

        // Advanced: Environmental Impact Calculation
        // Standard: 1 kWh Solar saves ~0.7 kg of CO2
        float co2Saved = totalGen * 0.7f;
        // Standard: 1 tree absorbs ~20 kg of CO2 per year
        float treesEquivalent = co2Saved / 20f;

        binding.tvCo2Offset.setText(String.format("%.1f kg", co2Saved));
        binding.tvTreesEquivalent.setText(String.format("%.2f", treesEquivalent));

        // Advanced: Data-Driven Insights
        updateInsights(totalGen, totalCons, overGenDays);

        setupBarChart(logs);
    }

    private void updateInsights(float gen, float cons, int overGenDays) {
        if (gen == 0) return;
        StringBuilder insight = new StringBuilder();
        float ratio = (gen / cons) * 100;

        if (ratio >= 100) {
            insight.append("🌟 Prosumer Alert! You generated ").append(String.format("%.0f%%", ratio))
                    .append(" of your needs. ");
            if (overGenDays > 15) insight.append("Excellent grid export consistency.");
            else insight.append("Try shifting more heavy appliances to 11AM-3PM.");
        } else if (ratio >= 50) {
            insight.append("⚡ Partially Independent. You're covering ").append(String.format("%.0f%%", ratio))
                    .append(" of usage via Solar. ");
            insight.append("Check if panels need cleaning to reach the 80% mark!");
        } else {
            insight.append("🔌 Grid Dependent. Usage is quite high. ");
            insight.append("Consider reducing AC usage during non-peak hours or checking for phantom loads.");
        }
        binding.tvInsightText.setText(insight.toString());
    }

    private void exportLogsToCSV() {
        if (currentLogs.isEmpty()) {
            Toast.makeText(this, "No data to export", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder csv = new StringBuilder("Date,Generated(kWh),Consumed(kWh),Battery(%),Weather,Savings(INR)\n");
        for (EnergyLog log : currentLogs) {
            csv.append(String.format("%s,%.2f,%.2f,%.0f,%s,%.2f\n",
                    log.date, log.generatedKwh, log.consumedKwh, log.batteryLevel, log.weatherCondition, log.netSavings));
        }

        try {
            File folder = new File(getCacheDir(), "exports");
            if (!folder.exists()) folder.mkdir();
            File file = new File(folder, "SuryaShakti_Report.csv");
            FileOutputStream out = new FileOutputStream(file);
            out.write(csv.toString().getBytes());
            out.close();

            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/csv");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Share Report via"));
        } catch (Exception e) {
            Toast.makeText(this, "Export failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupBarChart(List<EnergyLog> logs) {
        BarChart chart = binding.barChart;
        List<BarEntry> generatedEntries = new ArrayList<>();
        List<BarEntry> consumedEntries = new ArrayList<>();

        int start = Math.max(0, logs.size() - 7);
        for (int i = start; i < logs.size(); i++) {
            EnergyLog log = logs.get(i);
            int idx = i - start;
            generatedEntries.add(new BarEntry(idx * 2f, log.generatedKwh));
            consumedEntries.add(new BarEntry(idx * 2f + 0.5f, log.consumedKwh));
        }

        BarDataSet generatedSet = new BarDataSet(generatedEntries, "Generated");
        generatedSet.setColor(getColor(R.color.yellow_400));
        generatedSet.setValueTextColor(getThemeColor(android.R.attr.textColorSecondary));

        BarDataSet consumedSet = new BarDataSet(consumedEntries, "Consumed");
        consumedSet.setColor(getColor(R.color.red_400));
        consumedSet.setValueTextColor(getThemeColor(android.R.attr.textColorSecondary));

        BarData data = new BarData(generatedSet, consumedSet);
        data.setBarWidth(0.45f);

        int textColor = getThemeColor(android.R.attr.textColorSecondary);
        int surfaceColor = getThemeColor(com.google.android.material.R.attr.colorSurface);

        chart.setData(data);
        chart.setBackgroundColor(surfaceColor);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setTextColor(textColor);
        XAxis xAxis = chart.getXAxis();
        xAxis.setTextColor(textColor);
        xAxis.setGranularity(2f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getAxisLeft().setTextColor(textColor);
        chart.getAxisRight().setEnabled(false);
        chart.animateY(800);
        chart.invalidate();
    }

    @ColorInt
    private int getThemeColor(int attr) {
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(attr, typedValue, true);
        return typedValue.data;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
