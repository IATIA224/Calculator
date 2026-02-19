package com.calmahahh.app.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * Data Access Object for meal entries.
 */
@Dao
public interface MealEntryDao {

    @Insert
    long insert(MealEntry entry);

    @Insert
    void insertAll(List<MealEntry> entries);

    @Update
    void update(MealEntry entry);

    @Delete
    void delete(MealEntry entry);

    @Query("DELETE FROM meal_entries WHERE id = :id")
    void deleteById(long id);

    @Query("SELECT * FROM meal_entries WHERE date = :date ORDER BY mealType, id")
    List<MealEntry> getEntriesForDate(String date);

    @Query("SELECT * FROM meal_entries WHERE date = :date AND mealType = :mealType ORDER BY id")
    List<MealEntry> getEntriesForMeal(String date, String mealType);

    @Query("SELECT COALESCE(SUM(calories), 0) FROM meal_entries WHERE date = :date")
    double getTotalCaloriesForDate(String date);

    @Query("SELECT COALESCE(SUM(calories), 0) FROM meal_entries WHERE date = :date AND mealType = :mealType")
    double getMealCalories(String date, String mealType);

    @Query("SELECT COALESCE(SUM(protein), 0) FROM meal_entries WHERE date = :date")
    double getTotalProteinForDate(String date);

    @Query("SELECT COALESCE(SUM(carbs), 0) FROM meal_entries WHERE date = :date")
    double getTotalCarbsForDate(String date);

    @Query("SELECT COALESCE(SUM(fat), 0) FROM meal_entries WHERE date = :date")
    double getTotalFatForDate(String date);

    @Query("DELETE FROM meal_entries WHERE date = :date AND mealType = :mealType")
    void deleteAllForMeal(String date, String mealType);

    @Query("DELETE FROM meal_entries WHERE date = :date")
    void deleteAllForDate(String date);

    /** Get all dates that have entries, for calendar display */
    @Query("SELECT DISTINCT date FROM meal_entries ORDER BY date DESC")
    List<String> getAllDatesWithEntries();

    /** Get total calories per date for a date range (for calendar/charts) */
    @Query("SELECT date, COALESCE(SUM(calories), 0) as totalCalories FROM meal_entries " +
           "WHERE date BETWEEN :startDate AND :endDate GROUP BY date")
    List<DateCalories> getCaloriesInRange(String startDate, String endDate);

    /** Get average daily calories for a date range */
    @Query("SELECT COALESCE(AVG(dailyTotal), 0) FROM " +
           "(SELECT SUM(calories) as dailyTotal FROM meal_entries " +
           "WHERE date BETWEEN :startDate AND :endDate GROUP BY date)")
    double getAverageCaloriesInRange(String startDate, String endDate);
}
