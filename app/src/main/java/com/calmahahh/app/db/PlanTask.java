package com.calmahahh.app.db;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Represents a task within a plan on a specific day.
 */
@Entity(tableName = "plan_tasks",
        foreignKeys = @ForeignKey(
                entity = Plan.class,
                parentColumns = "id",
                childColumns = "planId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("planId")})
public class PlanTask {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private long planId;
    private String dayOfWeek; // "Monday", "Tuesday", ... "Sunday"
    private String taskName;
    private String category; // "Workout", "Chore", "Study", "Custom"
    private int sets;        // optional, default 0
    private int reps;        // optional, default 0
    private String intensity; // weight or difficulty description
    private String startTime; // HH:mm format
    private int durationMinutes;
    private String notes;
    private boolean completed;
    private boolean reminderEnabled;
    private long completedAt; // timestamp, 0 if not completed
    private int orderIndex;   // for ordering tasks within a day

    public PlanTask(long planId, String dayOfWeek, String taskName, String category) {
        this.planId = planId;
        this.dayOfWeek = dayOfWeek;
        this.taskName = taskName;
        this.category = category;
        this.sets = 0;
        this.reps = 0;
        this.intensity = "";
        this.startTime = "08:00";
        this.durationMinutes = 30;
        this.notes = "";
        this.completed = false;
        this.reminderEnabled = false;
        this.completedAt = 0;
        this.orderIndex = 0;
    }

    // Getters and setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getPlanId() { return planId; }
    public void setPlanId(long planId) { this.planId = planId; }

    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getSets() { return sets; }
    public void setSets(int sets) { this.sets = sets; }

    public int getReps() { return reps; }
    public void setReps(int reps) { this.reps = reps; }

    public String getIntensity() { return intensity; }
    public void setIntensity(String intensity) { this.intensity = intensity; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public boolean isReminderEnabled() { return reminderEnabled; }
    public void setReminderEnabled(boolean reminderEnabled) { this.reminderEnabled = reminderEnabled; }

    public long getCompletedAt() { return completedAt; }
    public void setCompletedAt(long completedAt) { this.completedAt = completedAt; }

    public int getOrderIndex() { return orderIndex; }
    public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }
}
