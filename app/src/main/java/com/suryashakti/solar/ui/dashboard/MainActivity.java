package com.suryashakti.solar.ui.dashboard;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.suryashakti.solar.R;
import com.suryashakti.solar.data.model.EnergyLog;
import com.suryashakti.solar.data.repository.EnergyViewModel;
import com.suryashakti.solar.databinding.ActivityMainBinding;
import com.suryashakti.solar.ui.care.SmartCareActivity;
import com.suryashakti.solar.ui.log.GenerationLogActivity;
import com.suryashakti.solar.ui.report.SavingsReportActivity;
import com.suryashakti.solar.ui.tips.TipsActivity;
import com.suryashakti.solar.ui.usage.ApplianceUsageActivity;
import com.suryashakti.solar.utils.NotificationHelper;
import com.suryashakti.solar.utils.ThemeManager;
import com.suryashakti.solar.utils.WeatherSimulator;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "SuryaShaktiPrefs";
    public static final String KEY_UNIT_PRICE = "unit_price";

    private ActivityMainBinding binding;
    private EnergyViewModel viewModel;
    private boolean isDarkTheme;
    private String selectedLogDate;

    private final ActivityResultLauncher<String> notifPermLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (!granted) Toast.makeText(this, "Enable notifications in Settings for alerts", Toast.LENGTH_LONG).show();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        isDarkTheme = ThemeManager.isDark(this);
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NotificationHelper.createNotificationChannel(this);
        requestNotificationPermissionIfNeeded();
        viewModel = new ViewModelProvider(this).get(EnergyViewModel.class);

        // Default to today
        selectedLogDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        setupThemeToggleButton();
        setupWeatherSelector();
        setupObservers();
        setupClickListeners();
        updateDateDisplay();
        updateEstimation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateEstimation();
    }

    private void updateDateDisplay() {
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(selectedLogDate);
            String display = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(date);
            binding.tvTodayDate.setText(display + " 📅");
        } catch (Exception e) {
            binding.tvTodayDate.setText(selectedLogDate + " 📅");
        }
    }

    private void setupThemeToggleButton() {
        binding.btnThemeToggle.setText(isDarkTheme ? "☀️ Light" : "🌙 Dark");
        binding.btnThemeToggle.setOnClickListener(v -> {
            ThemeManager.saveTheme(this, isDarkTheme ? ThemeManager.THEME_LIGHT : ThemeManager.THEME_DARK);
            recreate();
        });
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void setupWeatherSelector() {
        String[] weatherOptions = { WeatherSimulator.SUNNY, WeatherSimulator.PARTLY_CLOUDY, WeatherSimulator.CLOUDY };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.custom_spinner_item, weatherOptions);
        binding.autoWeather.setAdapter(adapter);
        binding.autoWeather.setText(weatherOptions[0], false);
        
        binding.autoWeather.setOnItemClickListener((parent, view, position, id) -> {
            updateEstimation();
        });
    }

    private void updateEstimation() {
        String weather = binding.autoWeather.getText().toString();
        SharedPreferences prefs = getSharedPreferences(SmartCareActivity.PREF_NAME, MODE_PRIVATE);
        float capacity = prefs.getFloat(SmartCareActivity.KEY_SYSTEM_CAPACITY, 2.0f);
        
        float estGen = WeatherSimulator.estimateDailyGeneration(weather, capacity);
        
        String resultText = String.format(Locale.getDefault(), 
                "~ %.1f kWh (%.1fkW panel, %s)", 
                estGen, capacity, weather);
        
        binding.tvEstimatedResult.setText(resultText);
    }

    private void setupObservers() {
        viewModel.getLatestLog().observe(this, log -> {
            if (log != null) updateDashboardUI(log);
            else resetDashboard();
        });

        // Use cumulative net energy instead of just today's
        viewModel.getTotalNetEnergy().observe(this, totalNet -> {
            float net = totalNet != null ? totalNet : 0;
            String gridText = String.format(Locale.getDefault(), "Grid Balance: %.2f kWh", net);
            binding.tvGridBalanceMain.setText(gridText);
            
            if (net >= 0) {
                binding.tvNetStatus.setText(String.format(Locale.getDefault(), "+%.2f kWh in Grid ⚡", net));
                binding.tvNetStatus.setTextColor(getColor(R.color.green_500));
                binding.tvGridBalanceMain.setTextColor(getColor(R.color.green_500));
            } else {
                binding.tvNetStatus.setText(String.format(Locale.getDefault(), "%.2f kWh from Grid 🔌", net));
                binding.tvNetStatus.setTextColor(getColor(R.color.red_400));
                binding.tvGridBalanceMain.setTextColor(getColor(R.color.red_400));
            }
        });

        viewModel.getTotalSavingsThisMonth().observe(this, total -> {
                float val = total != null ? total : 0;
                binding.tvMonthlySavings.setText(String.format("₹%.2f", val));
                binding.tvMonthlySavings.setTextColor(val >= 0 ? getColor(R.color.green_500) : getColor(R.color.red_400));
        });
    }

    private void updateDashboardUI(EnergyLog log) {
        float totalUsed = log.consumedKwh > 0 ? log.consumedKwh : 1;
        float solarRatio = Math.min(log.generatedKwh / totalUsed, 1.0f);
        int progressVal = (int) (solarRatio * 100);

        binding.circularProgress.setProgress(progressVal);
        binding.tvSolarPercent.setText(progressVal + "%");
        binding.tvGenerated.setText(String.format("%.2f kWh", log.generatedKwh));
        binding.tvConsumed.setText(String.format("%.2f kWh", log.consumedKwh));
        binding.tvBattery.setText(String.format("%.0f%%", log.batteryLevel));
        binding.batteryProgressBar.setProgress((int) log.batteryLevel);

        String weather = log.weatherCondition != null ? log.weatherCondition : WeatherSimulator.SUNNY;
        binding.tvWeatherIcon.setText(WeatherSimulator.getWeatherEmoji(weather));
        binding.tvWeatherLabel.setText(weather);
        
        binding.tvTodaySavings.setText(String.format("₹%.2f", log.netSavings));
        binding.tvTodaySavings.setTextColor(log.netSavings >= 0 ? getColor(R.color.green_500) : getColor(R.color.red_400));

        binding.tvIndependenceScore.setText(progressVal + "/100");
        binding.independenceProgressBar.setProgress(progressVal);
        if (progressVal >= 80) {
            binding.tvScoreLabel.setText("🌟 Energy Independent! (Prosumer)");
            binding.tvProsumerBadge.setVisibility(View.VISIBLE);
        } else if (progressVal >= 50) {
            binding.tvScoreLabel.setText("⚡ Partially Independent");
            binding.tvProsumerBadge.setVisibility(View.GONE);
        } else {
            binding.tvScoreLabel.setText("🔌 Grid Dependent");
            binding.tvProsumerBadge.setVisibility(View.GONE);
        }
    }

    private void resetDashboard() {
        binding.circularProgress.setProgress(0);
        binding.tvSolarPercent.setText("0%");
        binding.tvGenerated.setText("0.00 kWh");
        binding.tvConsumed.setText("0.00 kWh");
        binding.tvBattery.setText("0%");
        binding.batteryProgressBar.setProgress(0);
        binding.tvNetStatus.setText("No data yet");
        binding.tvNetStatus.setTextColor(getColor(R.color.yellow_400));
        binding.tvProsumerBadge.setVisibility(View.GONE);
        binding.tvTodaySavings.setText("₹0.00");
        binding.tvIndependenceScore.setText("0/100");
        binding.independenceProgressBar.setProgress(0);
        binding.tvScoreLabel.setText("🔌 Enter data for " + selectedLogDate);
    }

    private void setupClickListeners() {
        binding.tvTodayDate.setOnClickListener(v -> showDatePicker());
        binding.btnLog.setOnClickListener(v -> startActivity(new Intent(this, GenerationLogActivity.class)));
        binding.btnReport.setOnClickListener(v -> startActivity(new Intent(this, SavingsReportActivity.class)));
        binding.btnTips.setOnClickListener(v -> startActivity(new Intent(this, TipsActivity.class)));
        binding.btnAppliances.setOnClickListener(v -> startActivity(new Intent(this, ApplianceUsageActivity.class)));
        binding.btnSmartCare.setOnClickListener(v -> startActivity(new Intent(this, SmartCareActivity.class)));
        binding.btnSaveLog.setOnClickListener(v -> saveManualEntry());
        
        binding.btnEditCapacity.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences(SmartCareActivity.PREF_NAME, MODE_PRIVATE);
            float current = prefs.getFloat(SmartCareActivity.KEY_SYSTEM_CAPACITY, 2.0f);
            
            EditText input = new EditText(this);
            input.setHint("e.g. 5.0");
            input.setText(String.valueOf(current));
            input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
            
            new MaterialAlertDialogBuilder(this)
                .setTitle("System Capacity (kW)")
                .setMessage("Update your solar panel capacity.")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    try {
                        float val = Float.parseFloat(input.getText().toString());
                        prefs.edit().putFloat(SmartCareActivity.KEY_SYSTEM_CAPACITY, val).apply();
                        updateEstimation();
                    } catch (Exception e) {
                        Toast.makeText(this, "Invalid number", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
        });

        binding.btnEditRate.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            float currentRate = prefs.getFloat(KEY_UNIT_PRICE, 8.0f);
            
            EditText input = new EditText(this);
            input.setHint("e.g. 8.5");
            input.setText(String.valueOf(currentRate));
            input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
            
            new MaterialAlertDialogBuilder(this)
                .setTitle("Electricity Rate (₹/unit)")
                .setMessage("Set your current electricity price.")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    try {
                        float val = Float.parseFloat(input.getText().toString());
                        prefs.edit().putFloat(KEY_UNIT_PRICE, val).apply();
                        Toast.makeText(this, "Rate updated to ₹" + val, Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(this, "Invalid number", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
        });
    }

    private void showDatePicker() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Log Date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            calendar.setTimeInMillis(selection);
            selectedLogDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());
            updateDateDisplay();
            Toast.makeText(this, "Logging for: " + selectedLogDate, Toast.LENGTH_SHORT).show();
        });

        datePicker.show(getSupportFragmentManager(), "LOG_DATE_PICKER");
    }

    private void saveManualEntry() {
        String genStr  = binding.etGenerated.getText().toString().trim();
        String consStr = binding.etConsumed.getText().toString().trim();
        String battStr = binding.etBattery.getText().toString().trim();

        if (TextUtils.isEmpty(genStr))  { binding.etGenerated.setError("Enter generated kWh"); return; }
        if (TextUtils.isEmpty(consStr)) { binding.etConsumed.setError("Enter consumed kWh"); return; }
        if (TextUtils.isEmpty(battStr)) { binding.etBattery.setError("Enter battery level (0-100)"); return; }

        float generated, consumed, battery;
        try {
            generated = Float.parseFloat(genStr);
            consumed  = Float.parseFloat(consStr);
            battery   = Float.parseFloat(battStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numeric values only", Toast.LENGTH_SHORT).show();
            return;
        }

        if (generated < 0 || consumed < 0 || battery < 0 || battery > 100) {
            Toast.makeText(this, "Invalid input values", Toast.LENGTH_SHORT).show();
            return;
        }

        String weather = binding.autoWeather.getText().toString();
        if (TextUtils.isEmpty(weather)) weather = WeatherSimulator.SUNNY;
        
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        float currentRate = prefs.getFloat(KEY_UNIT_PRICE, 8.0f);
        
        EnergyLog log = new EnergyLog(selectedLogDate, generated, consumed, battery, weather, currentRate);
        viewModel.insert(log);

        if (log.overGeneration) NotificationHelper.sendOverGenerationAlert(this, generated - consumed);

        Toast.makeText(this, "✅ Log saved for " + selectedLogDate, Toast.LENGTH_SHORT).show();
        binding.etGenerated.setText("");
        binding.etConsumed.setText("");
        binding.etBattery.setText("");
    }
}
