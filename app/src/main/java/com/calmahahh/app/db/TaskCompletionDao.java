package com.calmahahh.app.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface TaskCompletionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(TaskCompletion completion);

    @Query("SELECT * FROM task_completions WHERE date = :date")
    List<TaskCompletion> getCompletionsForDate(String date);

    @Query("SELECT * FROM task_completions WHERE taskId = :taskId ORDER BY date DESC")
    List<TaskCompletion> getCompletionsForTask(long taskId);

    @Query("SELECT * FROM task_completions WHERE date BETWEEN :startDate AND :endDate ORDER BY date")
    List<TaskCompletion> getCompletionsInRange(String startDate, String endDate);

    @Query("SELECT COUNT(*) FROM task_completions WHERE completed = 1 AND date BETWEEN :startDate AND :endDate")
    int getCompletedCountInRange(String startDate, String endDate);

    @Query("SELECT COUNT(*) FROM task_completions WHERE date BETWEEN :startDate AND :endDate")
    int getTotalCountInRange(String startDate, String endDate);

    @Query("SELECT COUNT(*) FROM task_completions WHERE completed = 1")
    int getTotalCompleted();

    @Query("SELECT COUNT(*) FROM task_completions WHERE completed = 0 AND date < :today")
    int getMissedCount(String today);

    @Query("SELECT * FROM task_completions WHERE completed = 1 ORDER BY date DESC")
    List<TaskCompletion> getAllCompleted();

    @Query("SELECT DISTINCT date FROM task_completions WHERE completed = 1 ORDER BY date DESC")
    List<String> getCompletedDates();

    // Per-plan stats
    @Query("SELECT COUNT(*) FROM task_completions WHERE planName = :planName AND completed = 1 AND date BETWEEN :start AND :end")
    int getCompletedForPlanInRange(String planName, String start, String end);

    @Query("SELECT COUNT(*) FROM task_completions WHERE planName = :planName AND date BETWEEN :start AND :end")
    int getTotalForPlanInRange(String planName, String start, String end);

    @Query("DELETE FROM task_completions WHERE date = :date AND taskId = :taskId")
    void deleteCompletion(String date, long taskId);
}
