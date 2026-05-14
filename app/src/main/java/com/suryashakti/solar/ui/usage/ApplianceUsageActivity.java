package com.suryashakti.solar.ui.usage;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.suryashakti.solar.R;
import com.suryashakti.solar.data.model.Appliance;
import com.suryashakti.solar.data.repository.EnergyViewModel;
import com.suryashakti.solar.databinding.ActivityApplianceUsageBinding;
import com.suryashakti.solar.utils.ThemeManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ApplianceUsageActivity extends AppCompatActivity {

    private ActivityApplianceUsageBinding binding;
    private EnergyViewModel viewModel;
    private ApplianceAdapter adapter;
    private boolean isMonthlyMode = false;
    private List<Appliance> currentAppliances = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        binding = ActivityApplianceUsageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Energy Budget");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        viewModel = new ViewModelProvider(this).get(EnergyViewModel.class);
        setupRecyclerView();
        setupPieChart();

        // Use cumulative net energy (Grid Balance) for the runtime calculations
        viewModel.getTotalNetEnergy().observe(this, totalNet -> {
            float netSurplus = (totalNet != null) ? Math.max(0, totalNet) : 0;
            binding.tvAvailableEnergy.setText(String.format(Locale.getDefault(), "Grid Balance: %.2f kWh", netSurplus));
            adapter.updateBalance(netSurplus);
        });

        viewModel.getAllAppliances().observe(this, appliances -> {
            this.currentAppliances = appliances;
            adapter.setAppliances(appliances);
            updatePieChart(appliances);
        });

        binding.btnAddAppliance.setOnClickListener(v -> saveAppliance());

        binding.toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                isMonthlyMode = (checkedId == R.id.btn_month);
                adapter.setMonthlyMode(isMonthlyMode);
                updatePieChart(currentAppliances);
            }
        });

        // Quick Calculator Implementation
        binding.btnCalculateRuntime.setOnClickListener(v -> calculateQuickRuntime());
    }

    private void calculateQuickRuntime() {
        String inputStr = binding.etCalcKwh.getText().toString().trim();
        if (TextUtils.isEmpty(inputStr)) {
            binding.tvCalcResult.setText("Please enter a kWh value.");
            return;
        }

        try {
            float testKwh = Float.parseFloat(inputStr);
            if (currentAppliances == null || currentAppliances.isEmpty()) {
                binding.tvCalcResult.setText("Add some appliances first to see how long " + testKwh + " kWh lasts.");
                return;
            }

            float totalDailyKwh = 0;
            for (Appliance a : currentAppliances) {
                totalDailyKwh += a.watts;
            }

            if (totalDailyKwh <= 0) {
                binding.tvCalcResult.setText("Total appliance consumption is 0. Check your settings.");
                return;
            }

            float totalDays = testKwh / totalDailyKwh;
            String resultText;
            if (totalDays >= 1) {
                resultText = String.format(Locale.getDefault(), "With %.1f kWh, you can run ALL listed appliances for %.1f days.", testKwh, totalDays);
            } else {
                resultText = String.format(Locale.getDefault(), "With %.1f kWh, you can run ALL listed appliances for %.1f hours.", testKwh, totalDays * 24);
            }
            binding.tvCalcResult.setText(resultText);
            binding.tvCalcResult.setTextColor(getResources().getColor(R.color.yellow_400));

        } catch (NumberFormatException e) {
            binding.tvCalcResult.setText("Invalid number format.");
        }
    }

    private void saveAppliance() {
        String name = binding.etApplianceName.getText().toString().trim();
        String kwhStr = binding.etApplianceWatts.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            binding.etApplianceName.setError("Enter name");
            return;
        }
        if (TextUtils.isEmpty(kwhStr)) {
            binding.etApplianceWatts.setError("Enter kWh/day");
            return;
        }

        float powerKwhDay;
        try {
            powerKwhDay = Float.parseFloat(kwhStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid value", Toast.LENGTH_SHORT).show();
            return;
        }

        String emoji = "🔌";
        String lowerName = name.toLowerCase();
        if (lowerName.contains("fridge") || lowerName.contains("refrigerator")) emoji = "🧊";
        else if (lowerName.contains("ac") || lowerName.contains("air")) emoji = "❄️";
        else if (lowerName.contains("light") || lowerName.contains("bulb")) emoji = "💡";
        else if (lowerName.contains("tv") || lowerName.contains("television")) emoji = "📺";
        else if (lowerName.contains("fan")) emoji = "🌀";
        else if (lowerName.contains("wash")) emoji = "🧺";
        else if (lowerName.contains("iron")) emoji = "💨";
        else if (lowerName.contains("geyser") || lowerName.contains("heater")) emoji = "🔥";

        Appliance appliance = new Appliance(name, emoji, powerKwhDay);
        viewModel.insertAppliance(appliance);

        binding.etApplianceName.setText("");
        binding.etApplianceWatts.setText("");
        Toast.makeText(this, "Appliance added!", Toast.LENGTH_SHORT).show();
    }

    private void setupRecyclerView() {
        adapter = new ApplianceAdapter(appliance -> {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Delete Appliance")
                    .setMessage("Remove " + appliance.name + "?")
                    .setPositiveButton("Delete", (dialog, which) -> viewModel.deleteAppliance(appliance))
                    .setNegativeButton("Cancel", null)
                    .show();
        });
        binding.rvAppliances.setLayoutManager(new LinearLayoutManager(this));
        binding.rvAppliances.setAdapter(adapter);
    }

    private void setupPieChart() {
        binding.pieChart.getDescription().setEnabled(false);
        binding.pieChart.setHoleColor(Color.TRANSPARENT);
        binding.pieChart.setCenterTextColor(Color.GRAY);
        binding.pieChart.getLegend().setEnabled(false);
        binding.pieChart.setEntryLabelColor(Color.WHITE);
        binding.pieChart.setDrawEntryLabels(true);
        binding.pieChart.setHoleRadius(58f);
        binding.pieChart.setTransparentCircleRadius(61f);
    }

    private void updatePieChart(List<Appliance> appliances) {
        if (appliances == null || appliances.isEmpty()) {
            binding.pieChart.clear();
            binding.pieChart.setCenterText("No Data");
            return;
        }

        List<PieEntry> entries = new ArrayList<>();
        float totalKwh = 0;
        for (Appliance a : appliances) {
            float val = isMonthlyMode ? a.watts * 30 : a.watts;
            entries.add(new PieEntry(val, a.name));
            totalKwh += val;
        }

        String unit = isMonthlyMode ? "kWh/mo" : "kWh/day";
        binding.pieChart.setCenterText(String.format(Locale.getDefault(), "Total Budget\n%.2f\n%s", totalKwh, unit));
        binding.pieChart.setCenterTextSize(14f);

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(new int[]{
                Color.parseColor("#FBBF24"), Color.parseColor("#22C55E"),
                Color.parseColor("#60A5FA"), Color.parseColor("#F87171"),
                Color.parseColor("#A855F7"), Color.parseColor("#F472B6"),
                Color.parseColor("#10B981"), Color.parseColor("#3B82F6")
        });
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(10f);
        dataSet.setSliceSpace(2f);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter(binding.pieChart));
        binding.pieChart.setData(data);
        binding.pieChart.setUsePercentValues(true);
        binding.pieChart.animateY(800);
        binding.pieChart.invalidate();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }

    // --- Adapter ---
    static class ApplianceAdapter extends RecyclerView.Adapter<ApplianceAdapter.ViewHolder> {
        private List<Appliance> items = new ArrayList<>();
        private float currentGridBalance = 0;
        private boolean isMonthly = false;
        private final OnDeleteListener listener;

        interface OnDeleteListener { void onDelete(Appliance appliance); }

        ApplianceAdapter(OnDeleteListener listener) { this.listener = listener; }

        void setAppliances(List<Appliance> appliances) {
            this.items = appliances;
            notifyDataSetChanged();
        }

        void updateBalance(float balance) {
            this.currentGridBalance = balance;
            notifyDataSetChanged();
        }

        void setMonthlyMode(boolean isMonthly) {
            this.isMonthly = isMonthly;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_appliance_budget, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Appliance item = items.get(position);
            holder.tvName.setText(item.emoji + " " + item.name);
            
            float dailyVal = item.watts;
            float displayValue = isMonthly ? dailyVal * 30 : dailyVal;
            String unit = isMonthly ? "kWh/month" : "kWh/day";
            holder.tvWatts.setText(String.format(Locale.getDefault(), "%.2f %s", displayValue, unit));

            if (currentGridBalance > 0 && dailyVal > 0) {
                float totalDays = currentGridBalance / dailyVal;
                if (isMonthly) {
                    float months = totalDays / 30f;
                    holder.tvRuntime.setText(String.format(Locale.getDefault(), "Grid covers %.1f months", months));
                } else {
                    if (totalDays >= 1) {
                        holder.tvRuntime.setText(String.format(Locale.getDefault(), "Grid covers %.1f days", totalDays));
                    } else {
                        holder.tvRuntime.setText(String.format(Locale.getDefault(), "Grid covers %.0f hours", totalDays * 24));
                    }
                }
                holder.tvRuntime.setTextColor(Color.parseColor("#22C55E"));
            } else if (currentGridBalance <= 0) {
                holder.tvRuntime.setText("No grid surplus available");
                holder.tvRuntime.setTextColor(Color.parseColor("#F87171"));
            } else {
                holder.tvRuntime.setText("Enter consumption data");
                holder.tvRuntime.setTextColor(Color.parseColor("#94A3B8"));
            }

            holder.btnDelete.setOnClickListener(v -> listener.onDelete(item));
        }

        @Override
        public int getItemCount() { return items.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvWatts, tvRuntime;
            ImageButton btnDelete;
            ViewHolder(View v) {
                super(v);
                tvName = v.findViewById(R.id.tv_appliance_name);
                tvWatts = v.findViewById(R.id.tv_appliance_watts);
                tvRuntime = v.findViewById(R.id.tv_appliance_runtime);
                btnDelete = v.findViewById(R.id.btn_delete_appliance);
            }
        }
    }
}
