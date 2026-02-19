package com.calmahahh.app.db;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Records daily completion of a task for tracking history and statistics.
 * Each record represents one day's completion status of a specific task.
 */
@Entity(tableName = "task_completions",
        foreignKeys = @ForeignKey(
                entity = PlanTask.class,
                parentColumns = "id",
                childColumns = "taskId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("taskId"), @Index("date")})
public class TaskCompletion {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private long taskId;
    private String date; // yyyy-MM-dd
    private boolean completed;
    private long completedAt; // timestamp
    private String planName;
    private String taskName;
    private String category;

    public TaskCompletion(long taskId, String date, boolean completed) {
        this.taskId = taskId;
        this.date = date;
        this.completed = completed;
        this.completedAt = completed ? System.currentTimeMillis() : 0;
    }

    // Getters and setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getTaskId() { return taskId; }
    public void setTaskId(long taskId) { this.taskId = taskId; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public long getCompletedAt() { return completedAt; }
    public void setCompletedAt(long completedAt) { this.completedAt = completedAt; }

    public String getPlanName() { return planName; }
    public void setPlanName(String planName) { this.planName = planName; }

    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
