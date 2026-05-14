package com.suryashakti.solar.ui.care;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.suryashakti.solar.data.repository.EnergyViewModel;
import com.suryashakti.solar.databinding.ActivitySmartCareBinding;
import com.suryashakti.solar.utils.NotificationHelper;
import com.suryashakti.solar.utils.ThemeManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class SmartCareActivity extends AppCompatActivity {

    private ActivitySmartCareBinding binding;
    private EnergyViewModel viewModel;
    private SharedPreferences prefs;

    public static final String PREF_NAME = "smart_care_prefs";
    public static final String KEY_SYSTEM_COST = "system_cost";
    public static final String KEY_SYSTEM_CAPACITY = "system_capacity";
    public static final String KEY_LAST_CLEANED = "last_cleaned";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        binding = ActivitySmartCareBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Smart Care & ROI");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        viewModel = new ViewModelProvider(this).get(EnergyViewModel.class);
        prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        setupROI();
        setupForecast();
        setupMaintenance();

        binding.btnSendPeakAlert.setOnClickListener(v -> {
            NotificationHelper.sendPeakSunAlert(this);
            Toast.makeText(this, "Peak Sun Alert sent! 🔔", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupROI() {
        // ROI Calculation: Avoided Bill + Sale Earnings
        viewModel.getAvoidedCostLifetime().observe(this, avoided -> {
            float avoidedVal = avoided != null ? avoided : 0;
            viewModel.getTotalEarningsFromSales().observe(this, earnings -> {
                float earnVal = earnings != null ? earnings : 0;
                float saved = avoidedVal + earnVal;
                float cost = prefs.getFloat(KEY_SYSTEM_COST, 0);

                binding.tvTotalSavedRoi.setText(String.format(Locale.getDefault(), "Saved: ₹%.0f", saved));

                if (cost > 0) {
                    int progress = (int) Math.min((saved / cost) * 100, 100);
                    binding.paybackProgressBar.setProgress(progress);
                    binding.tvPaybackProgress.setText(String.format(Locale.getDefault(), "System is %d%% Paid Off", progress));
                    binding.tvRemainingCost.setText(String.format(Locale.getDefault(), "Left: ₹%.0f", Math.max(0, cost - saved)));
                } else {
                    binding.paybackProgressBar.setProgress(0);
                    binding.tvPaybackProgress.setText("Set system cost to track ROI");
                    binding.tvRemainingCost.setText("Left: ₹0");
                }
            });
        });

        binding.btnSetCost.setOnClickListener(v -> {
            EditText input = new EditText(this);
            input.setHint("e.g. 150000");
            float currentCost = prefs.getFloat(KEY_SYSTEM_COST, 0);
            input.setText(String.valueOf(currentCost));
            input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
            
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Total System Cost (₹)")
                    .setMessage("Enter the total amount paid for your solar setup.")
                    .setView(input)
                    .setPositiveButton("Save", (dialog, which) -> {
                        String val = input.getText().toString();
                        if (!val.isEmpty()) {
                            try {
                                prefs.edit().putFloat(KEY_SYSTEM_COST, Float.parseFloat(val)).apply();
                                setupROI(); // Refresh UI
                                Toast.makeText(this, "Cost updated!", Toast.LENGTH_SHORT).show();
                            } catch (NumberFormatException e) {
                                Toast.makeText(this, "Invalid number", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void setupForecast() {
        String[] forecasts = {
                "Tomorrow looks Sunny! ☀️\nHigh potential generation expected. Great day for heavy appliances.",
                "Tomorrow might be Partly Cloudy. ⛅\nStandard generation expected. Maintain normal usage.",
                "Rain/Clouds expected tomorrow. 🌧️\nLow generation. Try to minimize heavy battery usage."
        };
        int idx = (int) (Math.random() * forecasts.length);
        binding.tvForecastText.setText(forecasts[idx]);
    }

    private void setupMaintenance() {
        updateMaintenanceUI();

        binding.btnMarkCleaned.setOnClickListener(v -> {
            String today = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date());
            saveMaintenanceDate(today);
            Toast.makeText(this, "Great! Clean panels = High efficiency. 🧼", Toast.LENGTH_SHORT).show();
        });

        binding.btnDeleteMaintenance.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Delete Record")
                    .setMessage("Clear the last maintenance record?")
                    .setPositiveButton("Clear", (d, w) -> {
                        prefs.edit().remove(KEY_LAST_CLEANED).apply();
                        updateMaintenanceUI();
                        Toast.makeText(this, "Record deleted", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        binding.btnPickCleanedDate.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select Maintenance Date")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build();

            datePicker.addOnPositiveButtonClickListener(selection -> {
                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                calendar.setTimeInMillis(selection);
                String selected = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(calendar.getTime());
                saveMaintenanceDate(selected);
            });

            datePicker.show(getSupportFragmentManager(), "MAINTENANCE_DATE_PICKER");
        });
    }

    private void saveMaintenanceDate(String date) {
        prefs.edit().putString(KEY_LAST_CLEANED, date).apply();
        updateMaintenanceUI();
    }

    private void updateMaintenanceUI() {
        String lastCleaned = prefs.getString(KEY_LAST_CLEANED, "Not recorded");
        binding.tvLastCleaned.setText("Last cleaned: " + lastCleaned);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
