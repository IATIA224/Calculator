package com.calmahahh.app.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface PlanTaskDao {

    @Insert
    long insert(PlanTask task);

    @Insert
    void insertAll(List<PlanTask> tasks);

    @Update
    void update(PlanTask task);

    @Delete
    void delete(PlanTask task);

    @Query("SELECT * FROM plan_tasks WHERE planId = :planId ORDER BY dayOfWeek, orderIndex, startTime")
    List<PlanTask> getTasksForPlan(long planId);

    @Query("SELECT * FROM plan_tasks WHERE planId = :planId AND dayOfWeek = :day ORDER BY orderIndex, startTime")
    List<PlanTask> getTasksForPlanDay(long planId, String day);

    @Query("SELECT * FROM plan_tasks WHERE dayOfWeek = :day ORDER BY startTime")
    List<PlanTask> getTasksForDay(String day);

    @Query("SELECT * FROM plan_tasks WHERE id = :taskId")
    PlanTask getTaskById(long taskId);

    @Query("UPDATE plan_tasks SET completed = :completed, completedAt = :completedAt WHERE id = :taskId")
    void updateCompletion(long taskId, boolean completed, long completedAt);

    @Query("UPDATE plan_tasks SET reminderEnabled = :enabled WHERE id = :taskId")
    void updateReminderEnabled(long taskId, boolean enabled);

    @Query("SELECT COUNT(*) FROM plan_tasks WHERE planId = :planId")
    int getTaskCountForPlan(long planId);

    @Query("SELECT COUNT(*) FROM plan_tasks WHERE planId = :planId AND completed = 1")
    int getCompletedTaskCountForPlan(long planId);

    @Query("SELECT COUNT(*) FROM plan_tasks WHERE dayOfWeek = :day")
    int getTaskCountForDay(String day);

    @Query("SELECT COUNT(*) FROM plan_tasks WHERE dayOfWeek = :day AND completed = 1")
    int getCompletedTaskCountForDay(String day);

    @Query("DELETE FROM plan_tasks WHERE id = :taskId")
    void deleteById(long taskId);

    @Query("DELETE FROM plan_tasks WHERE planId = :planId")
    void deleteAllForPlan(long planId);

    @Query("SELECT * FROM plan_tasks WHERE reminderEnabled = 1")
    List<PlanTask> getTasksWithReminders();

    @Query("SELECT * FROM plan_tasks WHERE category = 'Workout' AND planId = :planId ORDER BY dayOfWeek, orderIndex")
    List<PlanTask> getWorkoutTasksForPlan(long planId);

    // Reset all completions for a new week
    @Query("UPDATE plan_tasks SET completed = 0, completedAt = 0")
    void resetAllCompletions();

    @Query("UPDATE plan_tasks SET completed = 0, completedAt = 0 WHERE planId = :planId")
    void resetCompletionsForPlan(long planId);
}
