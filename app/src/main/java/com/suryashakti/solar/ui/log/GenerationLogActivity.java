package com.suryashakti.solar.ui.log;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.suryashakti.solar.R;
import com.suryashakti.solar.data.model.EnergyLog;
import com.suryashakti.solar.data.repository.EnergyViewModel;
import com.suryashakti.solar.databinding.ActivityGenerationLogBinding;
import com.suryashakti.solar.utils.ThemeManager;
import com.suryashakti.solar.utils.WeatherSimulator;

import java.util.ArrayList;
import java.util.List;

public class GenerationLogActivity extends AppCompatActivity {

    private ActivityGenerationLogBinding binding;
    private EnergyViewModel viewModel;
    private LogAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        binding = ActivityGenerationLogBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Generation Log");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        viewModel = new ViewModelProvider(this).get(EnergyViewModel.class);
        adapter = new LogAdapter(log -> confirmDelete(log));

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        viewModel.getAllLogs().observe(this, logs -> {
            if (logs != null && !logs.isEmpty()) {
                adapter.setLogs(logs);
                binding.tvEmpty.setVisibility(View.GONE);
                binding.recyclerView.setVisibility(View.VISIBLE);
            } else {
                binding.tvEmpty.setVisibility(View.VISIBLE);
                binding.recyclerView.setVisibility(View.GONE);
            }
        });
    }

    private void confirmDelete(EnergyLog log) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete Entry")
                .setMessage("Delete log for " + log.date + "?")
                .setPositiveButton("Delete", (d, w) -> viewModel.deleteById(log.id))
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("Clear All History").setOnMenuItemClickListener(item -> {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Clear All History")
                    .setMessage("This will permanently delete all logs. Continue?")
                    .setPositiveButton("Clear All", (d, w) -> viewModel.deleteAll())
                    .setNegativeButton("Cancel", null)
                    .show();
            return true;
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }

    // ─── Adapter ─────────────────────────────────────────────────────────────
    static class LogAdapter extends RecyclerView.Adapter<LogAdapter.ViewHolder> {

        interface OnDeleteClick { void onDelete(EnergyLog log); }

        private List<EnergyLog> logs = new ArrayList<>();
        private final OnDeleteClick deleteCallback;

        LogAdapter(OnDeleteClick cb) { this.deleteCallback = cb; }

        void setLogs(List<EnergyLog> logs) {
            this.logs = logs;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_energy_log, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            EnergyLog log = logs.get(position);
            holder.tvDate.setText(log.date);
            holder.tvGenerated.setText(String.format("☀️ %.2f kWh", log.generatedKwh));
            holder.tvConsumed.setText(String.format("🔌 %.2f kWh", log.consumedKwh));
            holder.tvBattery.setText(String.format("🔋 %.0f%%", log.batteryLevel));
            holder.tvSavings.setText(String.format("₹%.2f", log.netSavings));
            holder.tvWeather.setText(WeatherSimulator.getWeatherEmoji(log.weatherCondition)
                    + " " + log.weatherCondition);
            holder.tvOverGen.setVisibility(log.overGeneration ? View.VISIBLE : View.GONE);
            holder.btnDelete.setOnClickListener(v -> deleteCallback.onDelete(log));
        }

        @Override
        public int getItemCount() { return logs.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvDate, tvGenerated, tvConsumed, tvBattery, tvSavings, tvWeather, tvOverGen;
            ImageButton btnDelete;

            ViewHolder(View view) {
                super(view);
                tvDate      = view.findViewById(R.id.tv_log_date);
                tvGenerated = view.findViewById(R.id.tv_log_generated);
                tvConsumed  = view.findViewById(R.id.tv_log_consumed);
                tvBattery   = view.findViewById(R.id.tv_log_battery);
                tvSavings   = view.findViewById(R.id.tv_log_savings);
                tvWeather   = view.findViewById(R.id.tv_log_weather);
                tvOverGen   = view.findViewById(R.id.tv_log_overgen);
                btnDelete   = view.findViewById(R.id.btn_delete_log);
            }
        }
    }
}
