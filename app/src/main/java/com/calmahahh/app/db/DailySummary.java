package com.calmahahh.app.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Room entity representing a daily calorie summary.
 * One row per date.
 */
@Entity(tableName = "daily_summary")
public class DailySummary {

    @PrimaryKey
    @NonNull
    public String date; // yyyy-MM-dd

    public double totalCalories;
    public double totalProtein;
    public double totalCarbs;
    public double totalFat;
    public int targetCalories;

    public DailySummary(@NonNull String date) {
        this.date = date;
    }
}
