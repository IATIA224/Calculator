package com.calmahahh.app.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Represents a plan (Workout Program, House Chores, Study Plan, etc.)
 */
@Entity(tableName = "plans")
public class Plan {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String name;
    private String type; // "workout", "chores", "study", "custom"
    private long createdAt;
    private long updatedAt;

    public Plan(String name, String type) {
        this.name = name;
        this.type = type;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // Getters and setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}
