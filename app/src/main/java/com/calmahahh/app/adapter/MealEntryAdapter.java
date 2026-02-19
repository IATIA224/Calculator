package com.calmahahh.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.calmahahh.app.R;
import com.calmahahh.app.db.MealEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying meal entries in a list, with edit/delete actions.
 */
public class MealEntryAdapter extends RecyclerView.Adapter<MealEntryAdapter.ViewHolder> {

    public interface OnEntryActionListener {
        void onEditEntry(MealEntry entry);
        void onDeleteEntry(MealEntry entry);
    }

    private final List<MealEntry> entries = new ArrayList<>();
    private OnEntryActionListener listener;

    public MealEntryAdapter(OnEntryActionListener listener) {
        this.listener = listener;
    }

    public void setEntries(List<MealEntry> newEntries) {
        entries.clear();
        entries.addAll(newEntries);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_meal_entry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MealEntry entry = entries.get(position);
        holder.tvFoodName.setText(entry.foodName);
        holder.tvCalories.setText(String.format(Locale.US, "%.0f kcal", entry.calories));
        holder.tvDetails.setText(String.format(Locale.US, "%.0fg | P:%.1f C:%.1f F:%.1f",
                entry.grams, entry.protein, entry.carbs, entry.fat));

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEditEntry(entry);
        });
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteEntry(entry);
        });
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    public double getTotalCalories() {
        double total = 0;
        for (MealEntry e : entries) total += e.calories;
        return total;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFoodName, tvCalories, tvDetails;
        ImageButton btnEdit, btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFoodName = itemView.findViewById(R.id.tvEntryFoodName);
            tvCalories = itemView.findViewById(R.id.tvEntryCalories);
            tvDetails = itemView.findViewById(R.id.tvEntryDetails);
            btnEdit = itemView.findViewById(R.id.btnEditEntry);
            btnDelete = itemView.findViewById(R.id.btnDeleteEntry);
        }
    }
}
