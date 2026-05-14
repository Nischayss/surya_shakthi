package com.suryashakti.solar.ui.sales;

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

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.suryashakti.solar.R;
import com.suryashakti.solar.data.model.SaleTransaction;
import com.suryashakti.solar.data.repository.EnergyViewModel;
import com.suryashakti.solar.databinding.ActivityEnergySalesBinding;
import com.suryashakti.solar.utils.ThemeManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EnergySalesActivity extends AppCompatActivity {

    private ActivityEnergySalesBinding binding;
    private EnergyViewModel viewModel;
    private SalesAdapter adapter;
    private float currentAvailableGrid = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        binding = ActivityEnergySalesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Sell Energy");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        viewModel = new ViewModelProvider(this).get(EnergyViewModel.class);
        setupRecyclerView();

        viewModel.getTotalNetEnergy().observe(this, balance -> {
            currentAvailableGrid = (balance != null) ? balance : 0;
            binding.tvAvailableToSell.setText(String.format(Locale.getDefault(), "Available in Grid: %.2f kWh", currentAvailableGrid));
        });

        viewModel.getAllSales().observe(this, sales -> {
            adapter.setSales(sales);
        });

        viewModel.getTotalEarningsFromSales().observe(this, earnings -> {
            float total = (earnings != null) ? earnings : 0;
            binding.tvTotalEarnings.setText(String.format(Locale.getDefault(), "₹%.2f", total));
        });

        binding.btnConfirmSale.setOnClickListener(v -> handleSale());
    }

    private void handleSale() {
        String kwhStr = binding.etKwhToSell.getText().toString().trim();
        String priceStr = binding.etSellPrice.getText().toString().trim();

        if (TextUtils.isEmpty(kwhStr) || TextUtils.isEmpty(priceStr)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            float kwh = Float.parseFloat(kwhStr);
            float price = Float.parseFloat(priceStr);

            if (kwh <= 0 || price <= 0) {
                Toast.makeText(this, "Invalid values", Toast.LENGTH_SHORT).show();
                return;
            }

            if (kwh > currentAvailableGrid) {
                Toast.makeText(this, "Not enough energy in grid! Available: " + currentAvailableGrid + " kWh", Toast.LENGTH_LONG).show();
                return;
            }

            String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            SaleTransaction sale = new SaleTransaction(date, kwh, price);
            viewModel.insertSale(sale);

            binding.etKwhToSell.setText("");
            binding.etSellPrice.setText("");
            Toast.makeText(this, "Sale recorded successfully!", Toast.LENGTH_SHORT).show();

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Numeric values only", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupRecyclerView() {
        adapter = new SalesAdapter(sale -> {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Delete Sale Record")
                    .setMessage(String.format(Locale.getDefault(), "Remove this sale record of ₹%.2f?", sale.totalAmount))
                    .setPositiveButton("Delete", (dialog, which) -> viewModel.deleteSale(sale))
                    .setNegativeButton("Cancel", null)
                    .show();
        });
        binding.rvSales.setLayoutManager(new LinearLayoutManager(this));
        binding.rvSales.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }

    // --- Adapter ---
    static class SalesAdapter extends RecyclerView.Adapter<SalesAdapter.ViewHolder> {
        private List<SaleTransaction> items = new ArrayList<>();
        private final OnDeleteListener deleteListener;

        interface OnDeleteListener { void onDelete(SaleTransaction sale); }

        SalesAdapter(OnDeleteListener deleteListener) {
            this.deleteListener = deleteListener;
        }

        void setSales(List<SaleTransaction> sales) {
            this.items = sales;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sale_transaction, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            SaleTransaction item = items.get(position);
            holder.tvDate.setText(item.date);
            holder.tvDetails.setText(String.format(Locale.getDefault(), "Sold %.1f kWh at ₹%.1f/u", item.kwhSold, item.pricePerUnit));
            holder.tvAmount.setText(String.format(Locale.getDefault(), "+ ₹%.2f", item.totalAmount));
            holder.btnDelete.setOnClickListener(v -> deleteListener.onDelete(item));
        }

        @Override
        public int getItemCount() { return items.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvDate, tvDetails, tvAmount;
            ImageButton btnDelete;
            ViewHolder(View v) {
                super(v);
                tvDate = v.findViewById(R.id.tv_sale_date);
                tvDetails = v.findViewById(R.id.tv_sale_details);
                tvAmount = v.findViewById(R.id.tv_sale_amount);
                btnDelete = v.findViewById(R.id.btn_delete_sale);
            }
        }
    }
}
