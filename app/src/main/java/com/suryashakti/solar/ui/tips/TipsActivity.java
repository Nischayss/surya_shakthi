package com.suryashakti.solar.ui.tips;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import com.suryashakti.solar.utils.ThemeManager;

import com.suryashakti.solar.databinding.ActivityTipsBinding;

public class TipsActivity extends AppCompatActivity {

    private ActivityTipsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        binding = ActivityTipsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Solar Energy Tips");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        // Tips are static in layout XML
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
