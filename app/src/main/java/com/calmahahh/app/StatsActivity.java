package com.calmahahh.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.calmahahh.app.db.AppDatabase;
import com.calmahahh.app.db.DateCalories;
import com.calmahahh.app.db.MealEntry;
import com.calmahahh.app.db.MealEntryDao;
import com.calmahahh.app.model.UserProfile;
import com.calmahahh.app.view.BarChartView;
import com.calmahahh.app.view.CalendarGridView;
import com.google.android.material.appbar.MaterialToolbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Statistics screen with calendar view, daily stats, weekly chart,
 * and streak tracking.
 */
public class StatsActivity extends AppCompatActivity {

    private CalendarGridView calendarView;
    private BarChartView barChart;
    private TextView tvMonthLabel, tvSelectedDate;
    private TextView tvConsumedToday, tvRequiredToday, tvDifference, tvStatus;
    private TextView tvWeeklyAvg, tvMonthlyAvg;
    private TextView tvStreak, tvProgressPercent;
    private View progressBarFill;
    private View progressBarBg;

    private UserProfile userProfile;
    private MealEntryDao mealEntryDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private final SimpleDateFormat displayFormat = new SimpleDateFormat("EEE, MMM d, yyyy", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        userProfile = UserProfile.load(this);
        mealEntryDao = AppDatabase.getInstance(this).mealEntryDao();

        initViews();
        setupCalendar();
        loadMonthData();
        loadTodayStats();
        loadWeeklyChart();
        loadStreakData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMonthData();
        loadTodayStats();
        loadWeeklyChart();
        loadStreakData();
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        calendarView = findViewById(R.id.calendarView);
        barChart = findViewById(R.id.barChart);
        tvMonthLabel = findViewById(R.id.tvMonthLabel);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        tvConsumedToday = findViewById(R.id.tvConsumedToday);
        tvRequiredToday = findViewById(R.id.tvRequiredToday);
        tvDifference = findViewById(R.id.tvDifference);
        tvStatus = findViewById(R.id.tvStatus);
        tvWeeklyAvg = findViewById(R.id.tvWeeklyAvg);
        tvMonthlyAvg = findViewById(R.id.tvMonthlyAvg);
        tvStreak = findViewById(R.id.tvStreak);
        tvProgressPercent = findViewById(R.id.tvProgressPercent);
        progressBarFill = findViewById(R.id.progressBarFill);
        progressBarBg = findViewById(R.id.progressBarBg);

        findViewById(R.id.btnPrevMonth).setOnClickListener(v -> {
            calendarView.previousMonth();
            tvMonthLabel.setText(calendarView.getDisplayMonthLabel());
            loadMonthData();
        });

        findViewById(R.id.btnNextMonth).setOnClickListener(v -> {
            calendarView.nextMonth();
            tvMonthLabel.setText(calendarView.getDisplayMonthLabel());
            loadMonthData();
        });
    }

    private void setupCalendar() {
        calendarView.setTargetCalories(userProfile.getTargetCalories());
        tvMonthLabel.setText(calendarView.getDisplayMonthLabel());

        String today = dateFormat.format(Calendar.getInstance().getTime());
        calendarView.setSelectedDate(today);

        calendarView.setOnDateClickListener(date -> {
            calendarView.setSelectedDate(date);
            loadDateStats(date);

            // Open meal detail for that day
            Intent intent = new Intent(StatsActivity.this, MealDetailActivity.class);
            intent.putExtra("date", date);
            startActivity(intent);
        });
    }

    private void loadMonthData() {
        int year = calendarView.getDisplayYear();
        int month = calendarView.getDisplayMonthIndex();

        Calendar start = Calendar.getInstance();
        start.set(year, month, 1);
        String startDate = dateFormat.format(start.getTime());

        Calendar end = (Calendar) start.clone();
        end.set(Calendar.DAY_OF_MONTH, end.getActualMaximum(Calendar.DAY_OF_MONTH));
        String endDate = dateFormat.format(end.getTime());

        executor.execute(() -> {
            List<DateCalories> data = mealEntryDao.getCaloriesInRange(startDate, endDate);
            Map<String, Double> map = new HashMap<>();
            for (DateCalories dc : data) {
                map.put(dc.date, dc.totalCalories);
            }

            // Also get monthly average
            double monthlyAvg = mealEntryDao.getAverageCaloriesInRange(startDate, endDate);

            mainHandler.post(() -> {
                calendarView.setCalorieData(map);
                tvMonthlyAvg.setText(String.format(Locale.US, "%.0f kcal", monthlyAvg));
            });
        });
    }

    private void loadTodayStats() {
        String today = dateFormat.format(Calendar.getInstance().getTime());
        loadDateStats(today);
    }

    private void loadDateStats(String date) {
        executor.execute(() -> {
            double consumed = mealEntryDao.getTotalCaloriesForDate(date);
            int target = userProfile.getTargetCalories();
            double diff = consumed - target;

            mainHandler.post(() -> {
                try {
                    java.util.Date d = dateFormat.parse(date);
                    tvSelectedDate.setText(displayFormat.format(d));
                } catch (Exception e) {
                    tvSelectedDate.setText(date);
                }

                tvConsumedToday.setText(String.format(Locale.US, "%.0f kcal", consumed));
                tvRequiredToday.setText(String.format(Locale.US, "%,d kcal", target));
                tvDifference.setText(String.format(Locale.US, "%+.0f kcal", diff));

                if (diff > 0) {
                    tvStatus.setText("Calorie Surplus");
                    tvStatus.setTextColor(0xFFF44336);
                    tvDifference.setTextColor(0xFFF44336);
                } else if (consumed == 0) {
                    tvStatus.setText("No Data");
                    tvStatus.setTextColor(0xFF757575);
                    tvDifference.setTextColor(0xFF757575);
                } else {
                    tvStatus.setText("Calorie Deficit");
                    tvStatus.setTextColor(0xFF2196F3);
                    tvDifference.setTextColor(0xFF2196F3);
                }

                // Progress bar
                int percent = target > 0 ? (int) Math.min(100, (consumed / target) * 100) : 0;
                tvProgressPercent.setText(percent + "%");
                progressBarFill.post(() -> {
                    int totalWidth = progressBarBg.getWidth();
                    int fillWidth = (int) (totalWidth * Math.min(1.0, consumed / Math.max(1, target)));
                    progressBarFill.getLayoutParams().width = fillWidth;
                    progressBarFill.requestLayout();

                    if (percent > 110) {
                        progressBarFill.setBackgroundColor(0xFFF44336);
                    } else if (percent > 90) {
                        progressBarFill.setBackgroundColor(0xFF4CAF50);
                    } else {
                        progressBarFill.setBackgroundColor(0xFF2196F3);
                    }
                });
            });
        });
    }

    private void loadWeeklyChart() {
        Calendar cal = Calendar.getInstance();
        String endDate = dateFormat.format(cal.getTime());
        cal.add(Calendar.DAY_OF_YEAR, -6);
        String startDate = dateFormat.format(cal.getTime());

        executor.execute(() -> {
            List<DateCalories> data = mealEntryDao.getCaloriesInRange(startDate, endDate);
            double weeklyAvg = mealEntryDao.getAverageCaloriesInRange(startDate, endDate);

            // Build bar data for all 7 days
            Map<String, Double> map = new HashMap<>();
            for (DateCalories dc : data) {
                map.put(dc.date, dc.totalCalories);
            }

            List<BarChartView.BarData> bars = new ArrayList<>();
            Calendar c = Calendar.getInstance();
            c.add(Calendar.DAY_OF_YEAR, -6);
            SimpleDateFormat shortFormat = new SimpleDateFormat("EEE", Locale.US);

            for (int i = 0; i < 7; i++) {
                String dateStr = dateFormat.format(c.getTime());
                String label = shortFormat.format(c.getTime());
                double value = map.containsKey(dateStr) ? map.get(dateStr) : 0;
                bars.add(new BarChartView.BarData(label, value));
                c.add(Calendar.DAY_OF_YEAR, 1);
            }

            mainHandler.post(() -> {
                barChart.setData(bars, userProfile.getTargetCalories());
                tvWeeklyAvg.setText(String.format(Locale.US, "%.0f kcal", weeklyAvg));
            });
        });
    }

    private void loadStreakData() {
        String today = dateFormat.format(Calendar.getInstance().getTime());
        executor.execute(() -> {
            // Count consecutive days within goal (simple approach)
            int streak = 0;
            Calendar cal = Calendar.getInstance();
            int target = userProfile.getTargetCalories();

            for (int i = 0; i < 365; i++) {
                String dateStr = dateFormat.format(cal.getTime());
                double cals = mealEntryDao.getTotalCaloriesForDate(dateStr);
                if (cals > 0 && cals <= target * 1.1) {
                    streak++;
                    cal.add(Calendar.DAY_OF_YEAR, -1);
                } else {
                    break;
                }
            }

            int finalStreak = streak;
            mainHandler.post(() -> {
                tvStreak.setText(finalStreak + " day" + (finalStreak != 1 ? "s" : ""));
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
