package com.calmahahh.app;

import android.Manifest;
import android.app.TimePickerDialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.calmahahh.app.adapter.TaskAdapter;
import com.calmahahh.app.db.AppDatabase;
import com.calmahahh.app.db.PlanTask;
import com.calmahahh.app.db.PlanTaskDao;
import com.calmahahh.app.db.TaskCompletion;
import com.calmahahh.app.db.TaskCompletionDao;
import com.calmahahh.app.db.WorkoutHistory;
import com.calmahahh.app.db.WorkoutHistoryDao;
import com.calmahahh.app.notification.TaskNotificationManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.tabs.TabLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlanDetailActivity extends AppCompatActivity implements TaskAdapter.OnTaskActionListener {

    private static final String[] DAYS = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

    private long planId;
    private String planName;
    private String selectedDay = DAYS[0];

    private RecyclerView recyclerTasks;
    private TextView tvEmptyDay;
    private TaskAdapter taskAdapter;
    private final List<PlanTask> tasks = new ArrayList<>();

    private PlanTaskDao planTaskDao;
    private TaskCompletionDao taskCompletionDao;
    private WorkoutHistoryDao workoutHistoryDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private ActivityResultLauncher<String> notificationPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_detail);

        // Register notification permission launcher
        notificationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
                        TaskNotificationManager.scheduleAllReminders(this);
                    } else {
                        Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        planId = getIntent().getLongExtra("planId", -1);
        planName = getIntent().getStringExtra("planName");

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(planName != null ? planName : "Plan Detail");
        toolbar.setNavigationOnClickListener(v -> finish());

        AppDatabase db = AppDatabase.getInstance(this);
        planTaskDao = db.planTaskDao();
        taskCompletionDao = db.taskCompletionDao();
        workoutHistoryDao = db.workoutHistoryDao();

        recyclerTasks = findViewById(R.id.recyclerTasks);
        tvEmptyDay = findViewById(R.id.tvEmptyDay);
        recyclerTasks.setLayoutManager(new LinearLayoutManager(this));
        taskAdapter = new TaskAdapter(tasks, this);
        recyclerTasks.setAdapter(taskAdapter);

        // Setup day tabs
        TabLayout tabDays = findViewById(R.id.tabDays);
        for (String day : DAYS) {
            tabDays.addTab(tabDays.newTab().setText(day));
        }

        // Select current day of week
        Calendar cal = Calendar.getInstance();
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int tabIndex = dayOfWeek == Calendar.SUNDAY ? 6 : dayOfWeek - 2;
        if (tabIndex >= 0 && tabIndex < 7) {
            tabDays.selectTab(tabDays.getTabAt(tabIndex));
            selectedDay = DAYS[tabIndex];
        }

        tabDays.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                selectedDay = DAYS[tab.getPosition()];
                loadTasks();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        FloatingActionButton fab = findViewById(R.id.fabAddTask);
        fab.setOnClickListener(v -> showAddEditTaskDialog(null));

        loadTasks();
    }

    private void loadTasks() {
        executor.execute(() -> {
            List<PlanTask> dayTasks = planTaskDao.getTasksForPlanDay(planId, selectedDay);
            mainHandler.post(() -> {
                tasks.clear();
                tasks.addAll(dayTasks);
                taskAdapter.notifyDataSetChanged();
                tvEmptyDay.setVisibility(tasks.isEmpty() ? View.VISIBLE : View.GONE);
                recyclerTasks.setVisibility(tasks.isEmpty() ? View.GONE : View.VISIBLE);
            });
        });
    }

    private void showAddEditTaskDialog(PlanTask existing) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_task, null);

        TextView tvTitle = dialogView.findViewById(R.id.tvDialogTitle);
        EditText etName = dialogView.findViewById(R.id.etTaskName);
        ChipGroup chipCategory = dialogView.findViewById(R.id.chipGroupCategory);
        View layoutWorkout = dialogView.findViewById(R.id.layoutWorkoutFields);
        EditText etSets = dialogView.findViewById(R.id.etSets);
        EditText etReps = dialogView.findViewById(R.id.etReps);
        EditText etIntensity = dialogView.findViewById(R.id.etIntensity);
        EditText etStartTime = dialogView.findViewById(R.id.etStartTime);
        EditText etEndTime = dialogView.findViewById(R.id.etEndTime);
        EditText etNotes = dialogView.findViewById(R.id.etNotes);
        SwitchMaterial switchReminder = dialogView.findViewById(R.id.switchReminder);

        // Show/hide workout fields based on category selection
        chipCategory.setOnCheckedChangeListener((group, checkedId) -> {
            layoutWorkout.setVisibility(checkedId == R.id.chipWorkout ? View.VISIBLE : View.GONE);
        });

        // Time picker for start time
        etStartTime.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new TimePickerDialog(this, (view, hourOfDay, minute) -> {
                etStartTime.setText(String.format(Locale.US, "%02d:%02d", hourOfDay, minute));
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show();
        });

        // Time picker for end time
        etEndTime.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new TimePickerDialog(this, (view, hourOfDay, minute) -> {
                etEndTime.setText(String.format(Locale.US, "%02d:%02d", hourOfDay, minute));
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show();
        });

        // Request notification permission when reminder is enabled
        switchReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                }
            }
        });

        if (existing != null) {
            tvTitle.setText("Edit Task");
            etName.setText(existing.getTaskName());
            etStartTime.setText(existing.getStartTime());
            // Calculate end time from start time and duration
            String endTime = calculateEndTime(existing.getStartTime(), existing.getDurationMinutes());
            etEndTime.setText(endTime);
            etNotes.setText(existing.getNotes());
            switchReminder.setChecked(existing.isReminderEnabled());

            switch (existing.getCategory()) {
                case "Workout": chipCategory.check(R.id.chipWorkout); break;
                case "Chore":   chipCategory.check(R.id.chipChore); break;
                case "Study":   chipCategory.check(R.id.chipStudy); break;
                default:        chipCategory.check(R.id.chipCustom); break;
            }

            if ("Workout".equals(existing.getCategory())) {
                layoutWorkout.setVisibility(View.VISIBLE);
                if (existing.getSets() > 0) etSets.setText(String.valueOf(existing.getSets()));
                if (existing.getReps() > 0) etReps.setText(String.valueOf(existing.getReps()));
                if (existing.getIntensity() != null && !existing.getIntensity().isEmpty()) {
                    etIntensity.setText(existing.getIntensity());
                }
            }
        } else {
            etStartTime.setText("08:00");
            etEndTime.setText("08:30");
        }

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton(existing != null ? "Save" : "Add", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(this, "Task name is required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String category;
                    int checkedId = chipCategory.getCheckedChipId();
                    if (checkedId == R.id.chipWorkout) category = "Workout";
                    else if (checkedId == R.id.chipChore) category = "Chore";
                    else if (checkedId == R.id.chipStudy) category = "Study";
                    else category = "Custom";

                    int sets = parseIntSafe(etSets.getText().toString());
                    int reps = parseIntSafe(etReps.getText().toString());
                    String intensity = etIntensity.getText().toString().trim();
                    String startTime = etStartTime.getText().toString().trim();
                    String endTime = etEndTime.getText().toString().trim();
                    int duration = calculateDurationMinutes(startTime, endTime);
                    if (duration <= 0) duration = 30;
                    String notes = etNotes.getText().toString().trim();
                    boolean reminder = switchReminder.isChecked();

                    final int finalDuration = duration;

                    executor.execute(() -> {
                        if (existing != null) {
                            existing.setTaskName(name);
                            existing.setCategory(category);
                            existing.setSets(sets);
                            existing.setReps(reps);
                            existing.setIntensity(intensity);
                            existing.setStartTime(startTime);
                            existing.setDurationMinutes(finalDuration);
                            existing.setNotes(notes);
                            existing.setReminderEnabled(reminder);
                            planTaskDao.update(existing);
                        } else {
                            PlanTask task = new PlanTask(planId, selectedDay, name, category);
                            task.setSets(sets);
                            task.setReps(reps);
                            task.setIntensity(intensity);
                            task.setStartTime(startTime);
                            task.setDurationMinutes(finalDuration);
                            task.setNotes(notes);
                            task.setReminderEnabled(reminder);
                            task.setOrderIndex(tasks.size());
                            long taskId = planTaskDao.insert(task);
                            task.setId(taskId);
                        }

                        // Schedule/cancel notification
                        mainHandler.post(() -> {
                            if (reminder) {
                                // Notifications are scheduled via TaskNotificationManager
                                TaskNotificationManager.scheduleAllReminders(this);
                            }
                            loadTasks();
                        });
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private int parseIntSafe(String s) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return 0; }
    }

    private String calculateEndTime(String startTime, int durationMinutes) {
        try {
            String[] parts = startTime.split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            minute += durationMinutes;
            hour += minute / 60;
            minute = minute % 60;
            hour = hour % 24;
            return String.format(Locale.US, "%02d:%02d", hour, minute);
        } catch (Exception e) {
            return startTime;
        }
    }

    private int calculateDurationMinutes(String startTime, String endTime) {
        try {
            String[] startParts = startTime.split(":");
            String[] endParts = endTime.split(":");
            int startHour = Integer.parseInt(startParts[0]);
            int startMin = Integer.parseInt(startParts[1]);
            int endHour = Integer.parseInt(endParts[0]);
            int endMin = Integer.parseInt(endParts[1]);

            int startTotalMin = startHour * 60 + startMin;
            int endTotalMin = endHour * 60 + endMin;

            // If end time is earlier, assume it's next day
            if (endTotalMin <= startTotalMin) {
                endTotalMin += 24 * 60;
            }

            return endTotalMin - startTotalMin;
        } catch (Exception e) {
            return 30; // Default 30 min
        }
    }

    // TaskAdapter callbacks

    @Override
    public void onTaskChecked(PlanTask task, boolean isChecked) {
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

            // If workout task completed, record workout history
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

            mainHandler.post(this::loadTasks);
        });
    }

    @Override
    public void onEditTask(PlanTask task) {
        showAddEditTaskDialog(task);
    }

    @Override
    public void onDeleteTask(PlanTask task) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Task")
                .setMessage("Delete \"" + task.getTaskName() + "\"?")
                .setPositiveButton("Delete", (d, w) -> {
                    executor.execute(() -> {
                        planTaskDao.delete(task);
                        mainHandler.post(this::loadTasks);
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
