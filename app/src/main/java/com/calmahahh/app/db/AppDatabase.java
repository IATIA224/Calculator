package com.calmahahh.app.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * Room database for the CalMahAhh app.
 * Stores daily summaries, meal entries, plans, tasks, completions, and workout history.
 */
@Database(entities = {
        DailySummary.class,
        MealEntry.class,
        Plan.class,
        PlanTask.class,
        TaskCompletion.class,
        WorkoutHistory.class
}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract DailySummaryDao dailySummaryDao();
    public abstract MealEntryDao mealEntryDao();
    public abstract PlanDao planDao();
    public abstract PlanTaskDao planTaskDao();
    public abstract TaskCompletionDao taskCompletionDao();
    public abstract WorkoutHistoryDao workoutHistoryDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "calmahahh_database"
                    ).fallbackToDestructiveMigration()
                     .build();
                }
            }
        }
        return INSTANCE;
    }
}
