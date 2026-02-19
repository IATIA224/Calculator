package com.calmahahh.app.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface PlanDao {

    @Insert
    long insert(Plan plan);

    @Update
    void update(Plan plan);

    @Delete
    void delete(Plan plan);

    @Query("SELECT * FROM plans ORDER BY createdAt DESC")
    List<Plan> getAllPlans();

    @Query("SELECT * FROM plans WHERE id = :planId")
    Plan getPlanById(long planId);

    @Query("SELECT * FROM plans WHERE type = :type ORDER BY createdAt DESC")
    List<Plan> getPlansByType(String type);

    @Query("SELECT COUNT(*) FROM plans")
    int getPlanCount();

    @Query("DELETE FROM plans WHERE id = :planId")
    void deleteById(long planId);
}
