package com.calmahahh.app.db;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Records workout weight/reps history for progressive overload tracking.
 */
@Entity(tableName = "workout_history",
        foreignKeys = @ForeignKey(
                entity = PlanTask.class,
                parentColumns = "id",
                childColumns = "taskId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("taskId"), @Index("date")})
public class WorkoutHistory {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private long taskId;
    private String date; // yyyy-MM-dd
    private String exerciseName;
    private double weight; // in kg or lbs
    private int sets;
    private int reps;
    private String notes;

    public WorkoutHistory(long taskId, String date, String exerciseName, double weight, int sets, int reps) {
        this.taskId = taskId;
        this.date = date;
        this.exerciseName = exerciseName;
        this.weight = weight;
        this.sets = sets;
        this.reps = reps;
        this.notes = "";
    }

    // Getters and setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getTaskId() { return taskId; }
    public void setTaskId(long taskId) { this.taskId = taskId; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getExerciseName() { return exerciseName; }
    public void setExerciseName(String exerciseName) { this.exerciseName = exerciseName; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public int getSets() { return sets; }
    public void setSets(int sets) { this.sets = sets; }

    public int getReps() { return reps; }
    public void setReps(int reps) { this.reps = reps; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
