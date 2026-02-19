package com.calmahahh.app.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface WorkoutHistoryDao {

    @Insert
    long insert(WorkoutHistory history);

    @Query("SELECT * FROM workout_history WHERE taskId = :taskId ORDER BY date DESC")
    List<WorkoutHistory> getHistoryForTask(long taskId);

    @Query("SELECT * FROM workout_history WHERE exerciseName = :exerciseName ORDER BY date DESC")
    List<WorkoutHistory> getHistoryForExercise(String exerciseName);

    @Query("SELECT * FROM workout_history WHERE exerciseName = :exerciseName ORDER BY date DESC LIMIT 1")
    WorkoutHistory getLatestForExercise(String exerciseName);

    @Query("SELECT * FROM workout_history WHERE taskId = :taskId ORDER BY date DESC LIMIT 1")
    WorkoutHistory getLatestForTask(long taskId);

    @Query("SELECT * FROM workout_history WHERE date BETWEEN :startDate AND :endDate ORDER BY date")
    List<WorkoutHistory> getHistoryInRange(String startDate, String endDate);

    @Query("SELECT DISTINCT exerciseName FROM workout_history ORDER BY exerciseName")
    List<String> getAllExerciseNames();

    @Query("SELECT * FROM workout_history WHERE exerciseName = :exerciseName AND date BETWEEN :start AND :end ORDER BY date")
    List<WorkoutHistory> getExerciseHistoryInRange(String exerciseName, String start, String end);

    @Query("DELETE FROM workout_history WHERE id = :id")
    void deleteById(long id);
}
