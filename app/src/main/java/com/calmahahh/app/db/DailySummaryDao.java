package com.calmahahh.app.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

/**
 * Data Access Object for daily summaries.
 */
@Dao
public interface DailySummaryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdate(DailySummary summary);

    @Query("SELECT * FROM daily_summary WHERE date = :date")
    DailySummary getForDate(String date);

    @Query("SELECT * FROM daily_summary WHERE date BETWEEN :startDate AND :endDate ORDER BY date")
    List<DailySummary> getInRange(String startDate, String endDate);

    @Query("SELECT * FROM daily_summary ORDER BY date DESC LIMIT :limit")
    List<DailySummary> getRecent(int limit);

    @Query("DELETE FROM daily_summary WHERE date = :date")
    void delete(String date);

    /** Count consecutive days where calories were within goal range */
    @Query("SELECT COUNT(*) FROM daily_summary WHERE date <= :endDate " +
           "AND totalCalories <= targetCalories * 1.1 AND totalCalories > 0 " +
           "ORDER BY date DESC")
    int getStreakDays(String endDate);
}
