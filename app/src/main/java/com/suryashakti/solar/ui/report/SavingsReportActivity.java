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
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.suryashakti.solar.R;
import com.suryashakti.solar.data.model.EnergyLog;
import com.suryashakti.solar.data.repository.EnergyViewModel;
import com.suryashakti.solar.databinding.ActivitySavingsReportBinding;
import com.suryashakti.solar.utils.ThemeManager;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
            getSupportActionBar().setTitle("Energy Analytics");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        viewModel = new ViewModelProvider(this).get(EnergyViewModel.class);

        // 30-day logs observation
        viewModel.getLast30Days().observe(this, logs -> {
            this.currentLogs = logs;
            update30DaySummary(logs);
        });

        // Financials
        viewModel.getTotalEarningsFromSales().observe(this, earnings -> {
            float val = earnings != null ? earnings : 0;
            binding.tvSoldProfit.setText(String.format(Locale.getDefault(), "₹%.2f", val));
        });

        // Lifetime Stats
        viewModel.getTotalGeneratedLifetime().observe(this, gen -> {
            binding.tvLifetimeGen.setText(String.format(Locale.getDefault(), "%.1f kWh", gen != null ? gen : 0));
        });

        viewModel.getTotalKwhSold().observe(this, sold -> {
            binding.tvLifetimeSold.setText(String.format(Locale.getDefault(), "%.1f kWh", sold != null ? sold : 0));
        });

        // ROI Calculation: Avoided Bill + Sale Earnings
        viewModel.getAvoidedCostLifetime().observe(this, avoided -> {
            float avoidedVal = avoided != null ? avoided : 0;
            viewModel.getTotalEarningsFromSales().observe(this, earnings -> {
                float earnVal = earnings != null ? earnings : 0;
                binding.tvLifetimeFinancial.setText(String.format(Locale.getDefault(), "₹%.2f", avoidedVal + earnVal));
            });
        });

        binding.btnExportCsv.setOnClickListener(v -> exportLogsToCSV());
    }

    private void update30DaySummary(List<EnergyLog> logs) {
        if (logs == null || logs.isEmpty()) return;

        float totalGen = 0, totalCons = 0, totalSavings = 0;
        int overGenDays = 0;
        for (EnergyLog log : logs) {
            totalGen += log.generatedKwh;
            totalCons += log.consumedKwh;
            totalSavings += Math.max(0, log.netSavings);
            if (log.overGeneration) overGenDays++;
        }

        binding.tvTotalGenerated.setText(String.format(Locale.getDefault(), "%.1f kWh", totalGen));
        binding.tvTotalConsumed.setText(String.format(Locale.getDefault(), "%.1f kWh", totalCons));
        binding.tvTotalSavings.setText(String.format(Locale.getDefault(), "₹%.2f", totalSavings));

        // Environmental Impact
        float co2Saved = totalGen * 0.7f;
        float treesEquivalent = co2Saved / 20f;
        binding.tvCo2Offset.setText(String.format(Locale.getDefault(), "%.1f kg", co2Saved));
        binding.tvTreesEquivalent.setText(String.format(Locale.getDefault(), "%.2f", treesEquivalent));

        updateInsights(totalGen, totalCons, overGenDays);
        setupBarChart(logs);
    }

    private void updateInsights(float gen, float cons, int overGenDays) {
        if (gen == 0) return;
        StringBuilder insight = new StringBuilder();
        float ratio = cons > 0 ? (gen / cons) * 100 : 100;

        if (ratio >= 100) {
            insight.append("🌟 Prosumer identified! Your solar yield is ").append(String.format(Locale.getDefault(), "%.0f%%", ratio))
                    .append(" of consumption. ");
            if (overGenDays > 20) insight.append("Exceptional consistency. You are a prime candidate for more energy sales.");
            else insight.append("High surplus detected. Consider selling grid credits for profit.");
        } else if (ratio >= 60) {
            insight.append("⚡ High Efficiency. You cover ").append(String.format(Locale.getDefault(), "%.0f%%", ratio))
                    .append(" of your power needs. ");
            insight.append("Minor usage shifts to peak sun hours (12 PM - 3 PM) could eliminate your bill entirely.");
        } else {
            insight.append("🔌 Grid Dependent. Your solar covers ").append(String.format(Locale.getDefault(), "%.0f%%", ratio))
                    .append(" of usage. ");
            insight.append("Check for heavy loads during evening hours. Try scheduling laundry or cleaning for midday.");
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
            csv.append(String.format(Locale.getDefault(), "%s,%.2f,%.2f,%.0f,%s,%.2f\n",
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

        // Reverse for chronological order
        List<EnergyLog> chronoLogs = new ArrayList<>();
        for (int i = logs.size() - 1; i >= 0; i--) chronoLogs.add(logs.get(i));

        // Show up to 30 days
        int displayCount = Math.min(chronoLogs.size(), 30);
        int startIndex = chronoLogs.size() - displayCount;

        for (int i = 0; i < displayCount; i++) {
            EnergyLog log = chronoLogs.get(startIndex + i);
            generatedEntries.add(new BarEntry(i, log.generatedKwh));
            consumedEntries.add(new BarEntry(i, log.consumedKwh));
        }

        BarDataSet generatedSet = new BarDataSet(generatedEntries, "Generated (kWh)");
        generatedSet.setColor(getColor(R.color.yellow_400));
        generatedSet.setDrawValues(false);

        BarDataSet consumedSet = new BarDataSet(consumedEntries, "Expenditure (kWh)");
        consumedSet.setColor(getColor(R.color.red_400));
        consumedSet.setDrawValues(false);

        float groupSpace = 0.3f;
        float barSpace = 0.05f;
        float barWidth = 0.3f;

        BarData data = new BarData(generatedSet, consumedSet);
        data.setBarWidth(barWidth);

        int textColor = getThemeColor(android.R.attr.textColorSecondary);
        int surfaceColor = getThemeColor(com.google.android.material.R.attr.colorSurface);

        chart.setData(data);
        chart.groupBars(0f, groupSpace, barSpace);
        chart.setFitBars(true);
        
        chart.setBackgroundColor(surfaceColor);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setTextColor(textColor);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(true);

        XAxis xAxis = chart.getXAxis();
        xAxis.setTextColor(textColor);
        xAxis.setCenterAxisLabels(true);
        xAxis.setGranularity(1f);
        xAxis.setAxisMinimum(0f);
        xAxis.setAxisMaximum(displayCount);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelCount(Math.min(displayCount, 6)); // Avoid overcrowding labels

        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int idx = (int) value;
                if (idx >= 0 && idx < displayCount) {
                    String date = chronoLogs.get(startIndex + idx).date;
                    if (date.length() > 5) return date.substring(5); // Show MM-dd
                    return date;
                }
                return "";
            }
        });

        chart.getAxisLeft().setTextColor(textColor);
        chart.getAxisRight().setEnabled(false);
        chart.animateY(1000);
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
