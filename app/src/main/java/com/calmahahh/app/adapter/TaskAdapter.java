package com.calmahahh.app.adapter;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.calmahahh.app.R;
import com.calmahahh.app.db.PlanTask;

import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.ViewHolder> {

    public interface OnTaskActionListener {
        void onTaskChecked(PlanTask task, boolean isChecked);
        void onEditTask(PlanTask task);
        void onDeleteTask(PlanTask task);
    }

    private final List<PlanTask> tasks;
    private final OnTaskActionListener listener;

    public TaskAdapter(List<PlanTask> tasks, OnTaskActionListener listener) {
        this.tasks = tasks;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PlanTask task = tasks.get(position);

        holder.tvTaskName.setText(task.getTaskName());
        holder.cbCompleted.setOnCheckedChangeListener(null);
        holder.cbCompleted.setChecked(task.isCompleted());

        // Strikethrough if completed
        if (task.isCompleted()) {
            holder.tvTaskName.setPaintFlags(holder.tvTaskName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvTaskName.setAlpha(0.5f);
        } else {
            holder.tvTaskName.setPaintFlags(holder.tvTaskName.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvTaskName.setAlpha(1f);
        }

        holder.tvCategory.setText(task.getCategory());
        setCategoryColor(holder.tvCategory, task.getCategory());

        String endTime = calculateEndTime(task.getStartTime(), task.getDurationMinutes());
        holder.tvTime.setText(task.getStartTime() + " - " + endTime);

        // Show workout details if workout category
        if ("Workout".equals(task.getCategory()) && (task.getSets() > 0 || task.getReps() > 0)) {
            holder.tvWorkoutDetails.setVisibility(View.VISIBLE);
            String details = "";
            if (task.getSets() > 0) details += task.getSets() + " sets";
            if (task.getReps() > 0) details += (details.isEmpty() ? "" : " × ") + task.getReps() + " reps";
            if (task.getIntensity() != null && !task.getIntensity().isEmpty()) {
                details += " @ " + task.getIntensity();
            }
            holder.tvWorkoutDetails.setText(details);
        } else {
            holder.tvWorkoutDetails.setVisibility(View.GONE);
        }

        holder.cbCompleted.setOnCheckedChangeListener((buttonView, isChecked) ->
                listener.onTaskChecked(task, isChecked));
        holder.btnEditTask.setOnClickListener(v -> listener.onEditTask(task));
        holder.btnDeleteTask.setOnClickListener(v -> listener.onDeleteTask(task));
    }

    private void setCategoryColor(TextView tv, String category) {
        int color;
        switch (category) {
            case "Workout": color = 0xFF2196F3; break;
            case "Chore":   color = 0xFFFF9800; break;
            case "Study":   color = 0xFF9C27B0; break;
            default:        color = 0xFF4CAF50; break;
        }
        tv.getBackground().setTint(color);
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    private static String calculateEndTime(String startTime, int durationMinutes) {
        try {
            String[] parts = startTime.split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);

            minute += durationMinutes;
            hour += minute / 60;
            minute = minute % 60;
            hour = hour % 24;

            return String.format(Locale.US, "%02d:%02d", hour, minute);
        } catch (Exception e) {
            return startTime; // Fallback to start time if parsing fails
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbCompleted;
        TextView tvTaskName, tvCategory, tvTime, tvWorkoutDetails;
        ImageButton btnEditTask, btnDeleteTask;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cbCompleted = itemView.findViewById(R.id.cbCompleted);
            tvTaskName = itemView.findViewById(R.id.tvTaskName);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvWorkoutDetails = itemView.findViewById(R.id.tvWorkoutDetails);
            btnEditTask = itemView.findViewById(R.id.btnEditTask);
            btnDeleteTask = itemView.findViewById(R.id.btnDeleteTask);
        }
    }
}
