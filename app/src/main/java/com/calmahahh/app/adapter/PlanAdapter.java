package com.calmahahh.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.calmahahh.app.R;
import com.calmahahh.app.db.Plan;

import java.util.List;

public class PlanAdapter extends RecyclerView.Adapter<PlanAdapter.ViewHolder> {

    public interface OnPlanActionListener {
        void onPlanClick(Plan plan);
        void onDuplicate(Plan plan);
        void onDelete(Plan plan);
    }

    private final List<Plan> plans;
    private final OnPlanActionListener listener;
    private final java.util.Map<Long, Integer> taskCounts;

    public PlanAdapter(List<Plan> plans, java.util.Map<Long, Integer> taskCounts, OnPlanActionListener listener) {
        this.plans = plans;
        this.taskCounts = taskCounts;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_plan, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Plan plan = plans.get(position);
        holder.tvPlanName.setText(plan.getName());

        String typeLabel;
        int iconRes;
        switch (plan.getType()) {
            case "workout":
                typeLabel = "Workout Program";
                iconRes = android.R.drawable.ic_menu_sort_by_size; // strength/fitness icon
                break;
            case "chores":
                typeLabel = "House Chores";
                iconRes = android.R.drawable.ic_menu_agenda; // list/task icon
                break;
            case "study":
                typeLabel = "Study Plan";
                iconRes = android.R.drawable.ic_menu_edit; // edit/write icon
                break;
            default:
                typeLabel = "Custom Plan";
                iconRes = android.R.drawable.ic_menu_view; // settings/custom icon
                break;
        }
        holder.tvPlanType.setText(typeLabel);
        holder.ivPlanIcon.setImageResource(iconRes);

        Integer count = taskCounts.get(plan.getId());
        int c = count != null ? count : 0;
        holder.tvTaskCount.setText(c + " task" + (c != 1 ? "s" : "") + " across 7 days");

        holder.itemView.setOnClickListener(v -> listener.onPlanClick(plan));
        holder.btnDuplicate.setOnClickListener(v -> listener.onDuplicate(plan));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(plan));
    }

    @Override
    public int getItemCount() {
        return plans.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPlanName, tvPlanType, tvTaskCount;
        ImageView ivPlanIcon;
        ImageButton btnDuplicate, btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPlanName = itemView.findViewById(R.id.tvPlanName);
            tvPlanType = itemView.findViewById(R.id.tvPlanType);
            tvTaskCount = itemView.findViewById(R.id.tvTaskCount);
            ivPlanIcon = itemView.findViewById(R.id.tvPlanIcon);
            btnDuplicate = itemView.findViewById(R.id.btnDuplicate);
            btnDelete = itemView.findViewById(R.id.btnDeletePlan);
        }
    }
}
