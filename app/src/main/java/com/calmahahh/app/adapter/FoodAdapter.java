package com.calmahahh.app.adapter;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.calmahahh.app.R;
import com.calmahahh.app.model.FoodItem;

import java.util.List;
import java.util.Locale;

/**
 * RecyclerView adapter that displays detected food items.
 * Each card shows food name, confidence, editable portion (grams),
 * calculated calories and protein, and per-100 g reference values.
 */
public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {

    /** Callback fired whenever the user changes a portion size. */
    public interface OnPortionChangedListener {
        void onPortionChanged();
    }

    private final List<FoodItem> foodItems;
    private final OnPortionChangedListener listener;

    public FoodAdapter(List<FoodItem> foodItems, OnPortionChangedListener listener) {
        this.foodItems = foodItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_food, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        holder.bind(foodItems.get(position));
    }

    @Override
    public int getItemCount() {
        return foodItems.size();
    }

    // ---------------------------------------------------------------

    class FoodViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvFoodName;
        private final TextView tvConfidence;
        private final TextView tvCalories;
        private final TextView tvProtein;
        private final TextView tvCarbs;
        private final TextView tvFat;
        private final TextView tvPer100g;
        private final EditText etGrams;
        private TextWatcher activeWatcher;

        FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFoodName   = itemView.findViewById(R.id.tvFoodName);
            tvConfidence = itemView.findViewById(R.id.tvConfidence);
            tvCalories   = itemView.findViewById(R.id.tvCalories);
            tvProtein    = itemView.findViewById(R.id.tvProtein);
            tvCarbs      = itemView.findViewById(R.id.tvCarbs);
            tvFat        = itemView.findViewById(R.id.tvFat);
            tvPer100g    = itemView.findViewById(R.id.tvPer100g);
            etGrams      = itemView.findViewById(R.id.etGrams);
        }

        void bind(FoodItem item) {
            tvFoodName.setText(capitalize(item.getName()));
            tvConfidence.setText(String.format(Locale.US, "%.0f%%", item.getConfidence() * 100));

            tvPer100g.setText(String.format(Locale.US, "Per 100g: %.0f kcal | P %.1fg | C %.1fg | F %.1fg",
                    item.getCaloriesPer100g(), item.getProteinPer100g(),
                    item.getCarbsPer100g(), item.getFatPer100g()));

            updateNutritionLabels(item);

            // Remove any previous watcher before setting text to avoid recursive updates
            if (activeWatcher != null) {
                etGrams.removeTextChangedListener(activeWatcher);
            }
            etGrams.setText(String.valueOf((int) item.getGrams()));

            activeWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) { }

                @Override
                public void afterTextChanged(Editable s) {
                    double grams = 0;
                    try {
                        grams = Double.parseDouble(s.toString());
                    } catch (NumberFormatException ignored) { }

                    item.setGrams(grams);
                    updateNutritionLabels(item);

                    if (listener != null) {
                        listener.onPortionChanged();
                    }
                }
            };
            etGrams.addTextChangedListener(activeWatcher);
        }

        private void updateNutritionLabels(FoodItem item) {
            tvCalories.setText(String.format(Locale.US, "%.0f kcal", item.getCalculatedCalories()));
            tvProtein.setText(String.format(Locale.US, "%.1f g", item.getCalculatedProtein()));
            tvCarbs.setText(String.format(Locale.US, "%.1f g", item.getCalculatedCarbs()));
            tvFat.setText(String.format(Locale.US, "%.1f g", item.getCalculatedFat()));
        }

        private String capitalize(String text) {
            if (text == null || text.isEmpty()) return text;
            return text.substring(0, 1).toUpperCase() + text.substring(1);
        }
    }
}
