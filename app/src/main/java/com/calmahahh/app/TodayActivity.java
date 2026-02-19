package com.calmahahh.app;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.calmahahh.app.adapter.TodayTaskAdapter;
import com.calmahahh.app.db.AppDatabase;
import com.calmahahh.app.db.Plan;
import com.calmahahh.app.db.PlanDao;
import com.calmahahh.app.db.PlanTask;
import com.calmahahh.app.db.PlanTaskDao;
import com.calmahahh.app.db.TaskCompletion;
import com.calmahahh.app.db.TaskCompletionDao;
import com.calmahahh.app.db.WorkoutHistory;
import com.calmahahh.app.db.WorkoutHistoryDao;
import com.google.android.material.appbar.MaterialToolbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Today screen - shows all tasks from all plans for the current day of week.
 */
public class TodayActivity extends AppCompatActivity implements TodayTaskAdapter.OnTodayTaskListener {

    private static final String[] DAYS = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};

    private RecyclerView recyclerTodayTasks;
    private TextView tvDayName, tvDate, tvTotalTasks, tvCompletedTasks, tvRemainingTasks, tvNoTasks;
    private ProgressBar progressToday;

    private TodayTaskAdapter adapter;
    private final List<PlanTask> todayTasks = new ArrayList<>();
    private final List<String> planNames = new ArrayList<>();
    private final Map<Long, String> planNameMap = new HashMap<>();

    private PlanDao planDao;
    private PlanTaskDao planTaskDao;
    private TaskCompletionDao taskCompletionDao;
    private WorkoutHistoryDao workoutHistoryDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private String todayDayName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_today);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        AppDatabase db = AppDatabase.getInstance(this);
        planDao = db.planDao();
        planTaskDao = db.planTaskDao();
        taskCompletionDao = db.taskCompletionDao();
        workoutHistoryDao = db.workoutHistoryDao();

        tvDayName = findViewById(R.id.tvDayName);
        tvDate = findViewById(R.id.tvDate);
        tvTotalTasks = findViewById(R.id.tvTotalTasks);
        tvCompletedTasks = findViewById(R.id.tvCompletedTasks);
        tvRemainingTasks = findViewById(R.id.tvRemainingTasks);
        tvNoTasks = findViewById(R.id.tvNoTasks);
        progressToday = findViewById(R.id.progressToday);

        recyclerTodayTasks = findViewById(R.id.recyclerTodayTasks);
        recyclerTodayTasks.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TodayTaskAdapter(todayTasks, planNames, this);
        recyclerTodayTasks.setAdapter(adapter);

        // Detect current day
        Calendar cal = Calendar.getInstance();
        todayDayName = DAYS[cal.get(Calendar.DAY_OF_WEEK) - 1];

        tvDayName.setText(todayDayName);
        tvDate.setText(new SimpleDateFormat("MMMM d, yyyy", Locale.US).format(new Date()));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTodayTasks();
    }

    private void loadTodayTasks() {
        executor.execute(() -> {
            // Load all plans to build name map
            List<Plan> allPlans = planDao.getAllPlans();
            planNameMap.clear();
            for (Plan p : allPlans) {
                planNameMap.put(p.getId(), p.getName());
            }

            // Load tasks for today's day of week
            List<PlanTask> allTodayTasks = planTaskDao.getTasksForDay(todayDayName);

            // Build plan names list
            List<String> names = new ArrayList<>();
            for (PlanTask t : allTodayTasks) {
                String pName = planNameMap.get(t.getPlanId());
                names.add(pName != null ? pName : "Unknown Plan");
            }

            // Count completed
            int total = allTodayTasks.size();
            int completed = 0;
            for (PlanTask t : allTodayTasks) {
                if (t.isCompleted()) completed++;
            }
            final int c = completed;

            mainHandler.post(() -> {
                todayTasks.clear();
                todayTasks.addAll(allTodayTasks);
                planNames.clear();
                planNames.addAll(names);
                adapter.notifyDataSetChanged();

                tvTotalTasks.setText(String.valueOf(total));
                tvCompletedTasks.setText(String.valueOf(c));
                tvRemainingTasks.setText(String.valueOf(total - c));

                int percent = total > 0 ? (c * 100 / total) : 0;
                progressToday.setProgress(percent);

                tvNoTasks.setVisibility(total == 0 ? View.VISIBLE : View.GONE);
                recyclerTodayTasks.setVisibility(total == 0 ? View.GONE : View.VISIBLE);
            });
        });
    }

    @Override
    public void onTaskChecked(PlanTask task, boolean isChecked, String planName) {
        long completedAt = isChecked ? System.currentTimeMillis() : 0;
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());

        executor.execute(() -> {
            planTaskDao.updateCompletion(task.getId(), isChecked, completedAt);

            // Delete any existing completion record for this task on this date
            taskCompletionDao.deleteCompletion(today, task.getId());

            // Only insert if task is being completed (not unchecked)
            if (isChecked) {
                TaskCompletion completion = new TaskCompletion(task.getId(), today, true);
                completion.setPlanName(planName);
                completion.setTaskName(task.getTaskName());
                completion.setCategory(task.getCategory());
                taskCompletionDao.insert(completion);
            }

            // Record workout history if applicable
            if (isChecked && "Workout".equals(task.getCategory())) {
                double weight = 0;
                try {
                    weight = Double.parseDouble(task.getIntensity().replaceAll("[^0-9.]", ""));
                } catch (Exception ignored) {}

                WorkoutHistory history = new WorkoutHistory(
                        task.getId(), today, task.getTaskName(),
                        weight, task.getSets(), task.getReps());
                workoutHistoryDao.insert(history);
            }

            mainHandler.post(this::loadTodayTasks);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
