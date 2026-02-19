package com.calmahahh.app;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.calmahahh.app.adapter.MealEntryAdapter;
import com.calmahahh.app.db.AppDatabase;
import com.calmahahh.app.db.MealEntry;
import com.calmahahh.app.db.MealEntryDao;
import com.calmahahh.app.model.UserProfile;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Displays and manages meal entries for a specific date.
 * Shows Breakfast, Lunch, and Dinner with food entries.
 * Allows adding, editing, and deleting entries.
 */
public class MealDetailActivity extends AppCompatActivity implements MealEntryAdapter.OnEntryActionListener {

    private String date;
    private MealEntryDao mealEntryDao;
    private UserProfile userProfile;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private TextView tvDate, tvDayTotal, tvDayStatus;
    private RecyclerView rvBreakfast, rvLunch, rvDinner;
    private TextView tvBreakfastTotal, tvLunchTotal, tvDinnerTotal;
    private MealEntryAdapter breakfastAdapter, lunchAdapter, dinnerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_detail);

        date = getIntent().getStringExtra("date");
        if (date == null) {
            date = new SimpleDateFormat("yyyy-MM-dd", Locale.US)
                    .format(new java.util.Date());
        }

        mealEntryDao = AppDatabase.getInstance(this).mealEntryDao();
        userProfile = UserProfile.load(this);

        initViews();
        loadMeals();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMeals();
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        tvDate = findViewById(R.id.tvMealDate);
        tvDayTotal = findViewById(R.id.tvDayTotal);
        tvDayStatus = findViewById(R.id.tvDayStatus);

        // Format display date
        try {
            java.util.Date d = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(date);
            tvDate.setText(new SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.US).format(d));
        } catch (Exception e) {
            tvDate.setText(date);
        }

        // Breakfast
        rvBreakfast = findViewById(R.id.rvBreakfast);
        tvBreakfastTotal = findViewById(R.id.tvBreakfastTotal);
        rvBreakfast.setLayoutManager(new LinearLayoutManager(this));
        breakfastAdapter = new MealEntryAdapter(this);
        rvBreakfast.setAdapter(breakfastAdapter);

        // Lunch
        rvLunch = findViewById(R.id.rvLunch);
        tvLunchTotal = findViewById(R.id.tvLunchTotal);
        rvLunch.setLayoutManager(new LinearLayoutManager(this));
        lunchAdapter = new MealEntryAdapter(this);
        rvLunch.setAdapter(lunchAdapter);

        // Dinner
        rvDinner = findViewById(R.id.rvDinner);
        tvDinnerTotal = findViewById(R.id.tvDinnerTotal);
        rvDinner.setLayoutManager(new LinearLayoutManager(this));
        dinnerAdapter = new MealEntryAdapter(this);
        rvDinner.setAdapter(dinnerAdapter);

        // Add buttons
        findViewById(R.id.btnAddBreakfast).setOnClickListener(v -> showAddDialog("Breakfast"));
        findViewById(R.id.btnAddLunch).setOnClickListener(v -> showAddDialog("Lunch"));
        findViewById(R.id.btnAddDinner).setOnClickListener(v -> showAddDialog("Dinner"));
    }

    private void loadMeals() {
        executor.execute(() -> {
            List<MealEntry> breakfast = mealEntryDao.getEntriesForMeal(date, "Breakfast");
            List<MealEntry> lunch = mealEntryDao.getEntriesForMeal(date, "Lunch");
            List<MealEntry> dinner = mealEntryDao.getEntriesForMeal(date, "Dinner");
            double totalCal = mealEntryDao.getTotalCaloriesForDate(date);

            mainHandler.post(() -> {
                breakfastAdapter.setEntries(breakfast);
                lunchAdapter.setEntries(lunch);
                dinnerAdapter.setEntries(dinner);

                tvBreakfastTotal.setText(String.format(Locale.US, "%.0f kcal", breakfastAdapter.getTotalCalories()));
                tvLunchTotal.setText(String.format(Locale.US, "%.0f kcal", lunchAdapter.getTotalCalories()));
                tvDinnerTotal.setText(String.format(Locale.US, "%.0f kcal", dinnerAdapter.getTotalCalories()));

                tvDayTotal.setText(String.format(Locale.US, "Total: %.0f / %d kcal", totalCal, userProfile.getTargetCalories()));

                double diff = totalCal - userProfile.getTargetCalories();
                if (totalCal == 0) {
                    tvDayStatus.setText("No entries yet");
                    tvDayStatus.setTextColor(0xFF757575);
                } else if (diff > 0) {
                    tvDayStatus.setText(String.format(Locale.US, "Surplus: +%.0f kcal", diff));
                    tvDayStatus.setTextColor(0xFFF44336);
                } else {
                    tvDayStatus.setText(String.format(Locale.US, "Deficit: %.0f kcal", diff));
                    tvDayStatus.setTextColor(0xFF2196F3);
                }
            });
        });
    }

    private void showAddDialog(String mealType) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_food, null);
        EditText etName = dialogView.findViewById(R.id.etDialogFoodName);
        EditText etCalories = dialogView.findViewById(R.id.etDialogCalories);
        EditText etProtein = dialogView.findViewById(R.id.etDialogProtein);
        EditText etCarbs = dialogView.findViewById(R.id.etDialogCarbs);
        EditText etFat = dialogView.findViewById(R.id.etDialogFat);
        EditText etGrams = dialogView.findViewById(R.id.etDialogGrams);

        new AlertDialog.Builder(this)
                .setTitle("Add to " + mealType)
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(this, "Enter a food name", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double cal = parseDouble(etCalories.getText().toString(), 0);
                    double pro = parseDouble(etProtein.getText().toString(), 0);
                    double carbs = parseDouble(etCarbs.getText().toString(), 0);
                    double fat = parseDouble(etFat.getText().toString(), 0);
                    double grams = parseDouble(etGrams.getText().toString(), 100);

                    MealEntry entry = new MealEntry(date, mealType, name, cal, pro, carbs, fat, grams);
                    executor.execute(() -> {
                        mealEntryDao.insert(entry);
                        mainHandler.post(this::loadMeals);
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onEditEntry(MealEntry entry) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_food, null);
        EditText etName = dialogView.findViewById(R.id.etDialogFoodName);
        EditText etCalories = dialogView.findViewById(R.id.etDialogCalories);
        EditText etProtein = dialogView.findViewById(R.id.etDialogProtein);
        EditText etCarbs = dialogView.findViewById(R.id.etDialogCarbs);
        EditText etFat = dialogView.findViewById(R.id.etDialogFat);
        EditText etGrams = dialogView.findViewById(R.id.etDialogGrams);

        // Pre-fill
        etName.setText(entry.foodName);
        etCalories.setText(String.format(Locale.US, "%.0f", entry.calories));
        etProtein.setText(String.format(Locale.US, "%.1f", entry.protein));
        etCarbs.setText(String.format(Locale.US, "%.1f", entry.carbs));
        etFat.setText(String.format(Locale.US, "%.1f", entry.fat));
        etGrams.setText(String.format(Locale.US, "%.0f", entry.grams));

        new AlertDialog.Builder(this)
                .setTitle("Edit Entry")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    entry.foodName = etName.getText().toString().trim();
                    entry.calories = parseDouble(etCalories.getText().toString(), 0);
                    entry.protein = parseDouble(etProtein.getText().toString(), 0);
                    entry.carbs = parseDouble(etCarbs.getText().toString(), 0);
                    entry.fat = parseDouble(etFat.getText().toString(), 0);
                    entry.grams = parseDouble(etGrams.getText().toString(), 100);

                    executor.execute(() -> {
                        mealEntryDao.update(entry);
                        mainHandler.post(this::loadMeals);
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDeleteEntry(MealEntry entry) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Entry")
                .setMessage("Remove " + entry.foodName + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    executor.execute(() -> {
                        mealEntryDao.delete(entry);
                        mainHandler.post(this::loadMeals);
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private double parseDouble(String s, double defaultVal) {
        try {
            return Double.parseDouble(s.trim());
        } catch (Exception e) {
            return defaultVal;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
