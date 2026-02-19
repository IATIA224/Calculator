package com.calmahahh.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.calmahahh.app.adapter.PlanAdapter;
import com.calmahahh.app.db.AppDatabase;
import com.calmahahh.app.db.Plan;
import com.calmahahh.app.db.PlanDao;
import com.calmahahh.app.db.PlanTask;
import com.calmahahh.app.db.PlanTaskDao;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlannerActivity extends AppCompatActivity implements PlanAdapter.OnPlanActionListener {

    private RecyclerView recyclerPlans;
    private PlanAdapter planAdapter;
    private final List<Plan> plans = new ArrayList<>();
    private final Map<Long, Integer> taskCounts = new HashMap<>();

    private PlanDao planDao;
    private PlanTaskDao planTaskDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_planner);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        AppDatabase db = AppDatabase.getInstance(this);
        planDao = db.planDao();
        planTaskDao = db.planTaskDao();

        recyclerPlans = findViewById(R.id.recyclerPlans);
        recyclerPlans.setLayoutManager(new LinearLayoutManager(this));
        planAdapter = new PlanAdapter(plans, taskCounts, this);
        recyclerPlans.setAdapter(planAdapter);

        FloatingActionButton fab = findViewById(R.id.fabAddPlan);
        fab.setOnClickListener(v -> showCreatePlanDialog());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPlans();
    }

    private void loadPlans() {
        executor.execute(() -> {
            List<Plan> all = planDao.getAllPlans();
            Map<Long, Integer> counts = new HashMap<>();
            for (Plan p : all) {
                counts.put(p.getId(), planTaskDao.getTaskCountForPlan(p.getId()));
            }
            mainHandler.post(() -> {
                plans.clear();
                plans.addAll(all);
                taskCounts.clear();
                taskCounts.putAll(counts);
                planAdapter.notifyDataSetChanged();
            });
        });
    }

    private void showCreatePlanDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_create_plan, null);
        EditText etName = dialogView.findViewById(R.id.etPlanName);
        ChipGroup chipGroup = dialogView.findViewById(R.id.chipGroupType);

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Create", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(this, "Please enter a plan name", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String type;
                    int checkedId = chipGroup.getCheckedChipId();
                    if (checkedId == R.id.chipTypeWorkout) type = "workout";
                    else if (checkedId == R.id.chipTypeChores) type = "chores";
                    else if (checkedId == R.id.chipTypeStudy) type = "study";
                    else type = "custom";

                    executor.execute(() -> {
                        Plan plan = new Plan(name, type);
                        planDao.insert(plan);
                        mainHandler.post(this::loadPlans);
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onPlanClick(Plan plan) {
        Intent intent = new Intent(this, PlanDetailActivity.class);
        intent.putExtra("planId", plan.getId());
        intent.putExtra("planName", plan.getName());
        startActivity(intent);
    }

    @Override
    public void onDuplicate(Plan plan) {
        executor.execute(() -> {
            // Create new plan
            Plan newPlan = new Plan(plan.getName() + " (Copy)", plan.getType());
            long newPlanId = planDao.insert(newPlan);

            // Copy all tasks
            List<PlanTask> tasks = planTaskDao.getTasksForPlan(plan.getId());
            for (PlanTask task : tasks) {
                PlanTask copy = new PlanTask(newPlanId, task.getDayOfWeek(), task.getTaskName(), task.getCategory());
                copy.setSets(task.getSets());
                copy.setReps(task.getReps());
                copy.setIntensity(task.getIntensity());
                copy.setStartTime(task.getStartTime());
                copy.setDurationMinutes(task.getDurationMinutes());
                copy.setNotes(task.getNotes());
                copy.setReminderEnabled(task.isReminderEnabled());
                copy.setOrderIndex(task.getOrderIndex());
                planTaskDao.insert(copy);
            }

            mainHandler.post(() -> {
                Toast.makeText(this, "Plan duplicated!", Toast.LENGTH_SHORT).show();
                loadPlans();
            });
        });
    }

    @Override
    public void onDelete(Plan plan) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Plan")
                .setMessage("Delete \"" + plan.getName() + "\" and all its tasks?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    executor.execute(() -> {
                        planDao.delete(plan);
                        mainHandler.post(this::loadPlans);
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
