package com.calmahahh.app;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.calmahahh.app.db.AppDatabase;
import com.calmahahh.app.db.Plan;
import com.calmahahh.app.db.PlanDao;
import com.calmahahh.app.db.TaskCompletion;
import com.calmahahh.app.db.TaskCompletionDao;
import com.calmahahh.app.db.WorkoutHistory;
import com.calmahahh.app.db.WorkoutHistoryDao;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlannerStatsActivity extends AppCompatActivity {

    private TextView tvTotalCompleted, tvMissedTasks, tvCurrentStreak, tvWeeklyRate;
    private BarChart barChartWeekly;
    private PieChart pieChart;
    private LineChart lineChartProgress;
    private LinearLayout layoutPlanStats;
    private MaterialCardView cardProgressiveOverload;
    private Spinner spinnerExercise;
    private TextView tvProgressSuggestion;

    private PlanDao planDao;
    private TaskCompletionDao taskCompletionDao;
    private WorkoutHistoryDao workoutHistoryDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final List<String> exerciseNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_planner_stats);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        AppDatabase db = AppDatabase.getInstance(this);
        planDao = db.planDao();
        taskCompletionDao = db.taskCompletionDao();
        workoutHistoryDao = db.workoutHistoryDao();

        tvTotalCompleted = findViewById(R.id.tvTotalCompleted);
        tvMissedTasks = findViewById(R.id.tvMissedTasks);
        tvCurrentStreak = findViewById(R.id.tvCurrentStreak);
        tvWeeklyRate = findViewById(R.id.tvWeeklyRate);
        barChartWeekly = findViewById(R.id.barChartWeekly);
        pieChart = findViewById(R.id.pieChart);
        lineChartProgress = findViewById(R.id.lineChartProgress);
        layoutPlanStats = findViewById(R.id.layoutPlanStats);
        cardProgressiveOverload = findViewById(R.id.cardProgressiveOverload);
        spinnerExercise = findViewById(R.id.spinnerExercise);
        tvProgressSuggestion = findViewById(R.id.tvProgressSuggestion);

        loadStats();
    }

    private void loadStats() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());

        // Weekly range (Monday-Sunday of current week)
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        String weekStart = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.getTime());
        cal.add(Calendar.DAY_OF_WEEK, 6);
        String weekEnd = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.getTime());

        executor.execute(() -> {
            // Overview stats
            int totalCompleted = taskCompletionDao.getTotalCompleted();
            int missed = taskCompletionDao.getMissedCount(today);
            int streak = calculateStreak();

            // Weekly stats
            int weeklyCompleted = taskCompletionDao.getCompletedCountInRange(weekStart, weekEnd);
            int weeklyTotal = taskCompletionDao.getTotalCountInRange(weekStart, weekEnd);
            float weeklyRate = weeklyTotal > 0 ? (weeklyCompleted * 100f / weeklyTotal) : 0;

            // Daily breakdown for bar chart
            List<float[]> dailyData = new ArrayList<>();
            Calendar barCal = Calendar.getInstance();
            barCal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            String[] dayLabels = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
            for (int i = 0; i < 7; i++) {
                String dayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(barCal.getTime());
                int comp = taskCompletionDao.getCompletedCountInRange(dayDate, dayDate);
                int total = taskCompletionDao.getTotalCountInRange(dayDate, dayDate);
                dailyData.add(new float[]{comp, total});
                barCal.add(Calendar.DAY_OF_WEEK, 1);
            }

            // Per-plan stats
            List<Plan> plans = planDao.getAllPlans();
            List<float[]> planStats = new ArrayList<>();
            for (Plan p : plans) {
                int pComp = taskCompletionDao.getCompletedForPlanInRange(p.getName(), weekStart, weekEnd);
                int pTotal = taskCompletionDao.getTotalForPlanInRange(p.getName(), weekStart, weekEnd);
                planStats.add(new float[]{pComp, pTotal});
            }

            // Workout exercise names for progressive overload
            List<String> exercises = workoutHistoryDao.getAllExerciseNames();

            mainHandler.post(() -> {
                // Overview
                tvTotalCompleted.setText(String.valueOf(totalCompleted));
                tvMissedTasks.setText(String.valueOf(missed));
                tvCurrentStreak.setText(String.valueOf(streak));
                tvWeeklyRate.setText(String.format(Locale.US, "%.0f%% completion rate", weeklyRate));

                // Bar chart
                setupBarChart(dailyData, dayLabels);

                // Pie chart
                setupPieChart(totalCompleted, missed);

                // Per-plan stats
                setupPlanStats(plans, planStats);

                // Progressive overload
                exerciseNames.clear();
                exerciseNames.addAll(exercises);
                if (!exercises.isEmpty()) {
                    cardProgressiveOverload.setVisibility(View.VISIBLE);
                    setupExerciseSpinner();
                }
            });
        });
    }

    private int calculateStreak() {
        List<String> completedDates = taskCompletionDao.getCompletedDates();
        if (completedDates.isEmpty()) return 0;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        int streak = 0;
        Calendar cal = Calendar.getInstance();

        for (int i = 0; i < 365; i++) {
            String date = sdf.format(cal.getTime());
            if (completedDates.contains(date)) {
                streak++;
                cal.add(Calendar.DAY_OF_YEAR, -1);
            } else {
                break;
            }
        }
        return streak;
    }

    private void setupBarChart(List<float[]> dailyData, String[] dayLabels) {
        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < dailyData.size(); i++) {
            float completed = dailyData.get(i)[0];
            entries.add(new BarEntry(i, completed));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Completed Tasks");
        dataSet.setColor(Color.parseColor("#4CAF50"));
        dataSet.setValueTextSize(10f);

        BarData barData = new BarData(dataSet);
        barChartWeekly.setData(barData);

        XAxis xAxis = barChartWeekly.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(dayLabels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);

        barChartWeekly.getAxisRight().setEnabled(false);
        barChartWeekly.getDescription().setEnabled(false);
        barChartWeekly.getLegend().setEnabled(false);
        barChartWeekly.setFitBars(true);
        barChartWeekly.animateY(600);
        barChartWeekly.invalidate();
    }

    private void setupPieChart(int completed, int missed) {
        List<PieEntry> entries = new ArrayList<>();
        if (completed > 0) entries.add(new PieEntry(completed, "Completed"));
        if (missed > 0) entries.add(new PieEntry(missed, "Missed"));

        if (entries.isEmpty()) {
            entries.add(new PieEntry(1, "No Data"));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(
                Color.parseColor("#4CAF50"),
                Color.parseColor("#F44336"),
                Color.parseColor("#BDBDBD")
        );
        dataSet.setValueTextSize(14f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueFormatter(new PercentFormatter(pieChart));

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setHoleRadius(40f);
        pieChart.setTransparentCircleRadius(45f);
        pieChart.setCenterText("Tasks");
        pieChart.animateY(600);
        pieChart.invalidate();
    }

    private void setupPlanStats(List<Plan> plans, List<float[]> planStats) {
        layoutPlanStats.removeAllViews();

        for (int i = 0; i < plans.size(); i++) {
            Plan plan = plans.get(i);
            float[] stats = planStats.get(i);
            int completed = (int) stats[0];
            int total = (int) stats[1];
            float rate = total > 0 ? (completed * 100f / total) : 0;

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.VERTICAL);
            row.setPadding(0, 8, 0, 8);

            // Plan name + rate
            LinearLayout header = new LinearLayout(this);
            header.setOrientation(LinearLayout.HORIZONTAL);

            TextView tvName = new TextView(this);
            tvName.setText(plan.getName());
            tvName.setTextSize(14);
            tvName.setTextColor(Color.parseColor("#212121"));
            LinearLayout.LayoutParams lpName = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            tvName.setLayoutParams(lpName);

            TextView tvRate = new TextView(this);
            tvRate.setText(String.format(Locale.US, "%.0f%% (%d/%d)", rate, completed, total));
            tvRate.setTextSize(13);
            tvRate.setTextColor(Color.parseColor("#4CAF50"));

            header.addView(tvName);
            header.addView(tvRate);

            // Progress bar
            ProgressBar pb = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
            pb.setMax(100);
            pb.setProgress((int) rate);
            LinearLayout.LayoutParams lpPb = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 8);
            lpPb.topMargin = 4;
            pb.setLayoutParams(lpPb);

            row.addView(header);
            row.addView(pb);
            layoutPlanStats.addView(row);
        }
    }

    private void setupExerciseSpinner() {
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, exerciseNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerExercise.setAdapter(spinnerAdapter);

        spinnerExercise.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadProgressChart(exerciseNames.get(position));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadProgressChart(String exerciseName) {
        // Get last 8 weeks of data
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.WEEK_OF_YEAR, -8);
        String startDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.getTime());
        String endDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());

        executor.execute(() -> {
            List<WorkoutHistory> history = workoutHistoryDao.getExerciseHistoryInRange(exerciseName, startDate, endDate);

            mainHandler.post(() -> {
                List<Entry> weightEntries = new ArrayList<>();
                List<Entry> repEntries = new ArrayList<>();
                List<String> labels = new ArrayList<>();

                for (int i = 0; i < history.size(); i++) {
                    WorkoutHistory h = history.get(i);
                    weightEntries.add(new Entry(i, (float) h.getWeight()));
                    repEntries.add(new Entry(i, h.getReps()));
                    labels.add(h.getDate().substring(5)); // MM-dd
                }

                if (weightEntries.isEmpty()) {
                    lineChartProgress.clear();
                    lineChartProgress.invalidate();
                    tvProgressSuggestion.setVisibility(View.GONE);
                    return;
                }

                LineDataSet weightSet = new LineDataSet(weightEntries, "Weight (kg)");
                weightSet.setColor(Color.parseColor("#2196F3"));
                weightSet.setCircleColor(Color.parseColor("#2196F3"));
                weightSet.setLineWidth(2f);
                weightSet.setValueTextSize(10f);

                LineDataSet repSet = new LineDataSet(repEntries, "Reps");
                repSet.setColor(Color.parseColor("#FF9800"));
                repSet.setCircleColor(Color.parseColor("#FF9800"));
                repSet.setLineWidth(2f);
                repSet.setValueTextSize(10f);

                LineData lineData = new LineData(weightSet, repSet);
                lineChartProgress.setData(lineData);

                XAxis xAxis = lineChartProgress.getXAxis();
                xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                xAxis.setGranularity(1f);

                lineChartProgress.getAxisRight().setEnabled(false);
                lineChartProgress.getDescription().setEnabled(false);
                lineChartProgress.animateX(600);
                lineChartProgress.invalidate();

                // Progressive overload suggestion
                if (history.size() >= 2) {
                    WorkoutHistory latest = history.get(history.size() - 1);
                    WorkoutHistory prev = history.get(history.size() - 2);

                    if (latest.getWeight() >= prev.getWeight() && latest.getReps() >= prev.getReps()) {
                        tvProgressSuggestion.setVisibility(View.VISIBLE);
                        double suggestedWeight = latest.getWeight() * 1.05; // 5% increase
                        tvProgressSuggestion.setText(String.format(Locale.US,
                                "💪 Great progress! Consider increasing weight to %.1f kg next session.",
                                suggestedWeight));
                    } else {
                        tvProgressSuggestion.setVisibility(View.GONE);
                    }
                }
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
