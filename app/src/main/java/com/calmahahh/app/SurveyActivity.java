package com.calmahahh.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.calmahahh.app.model.UserProfile;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.card.MaterialCardView;

import java.util.Locale;

/**
 * Survey that collects user info for TDEE calculation.
 * Can be used for first-run or to edit existing profile.
 * Shows a live preview of the calculated daily calorie target.
 */
public class SurveyActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private MaterialButtonToggleGroup toggleGender, toggleGoal;
    private EditText etAge, etWeight, etHeight, etBodyFat;
    private RadioGroup rgActivity;
    private TextView tvTargetPreview, tvTDEEInfo;
    private MaterialCardView cardPreview;
    private MaterialButton btnSave;
    private boolean isFirstRun;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey);

        // Check if this is first run or edit mode
        isFirstRun = !UserProfile.isSurveyCompleted(this);

        initViews();
        setupToolbar();
        preloadExistingData();
        setupListeners();
    }

    private void setupToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        if (!isFirstRun) {
            // Show back button for edit mode
            toolbar.setNavigationOnClickListener(v -> finish());
        } else {
            // Hide back button for first run
            toolbar.setNavigationIcon(null);
        }
    }

    private void preloadExistingData() {
        if (isFirstRun) return;

        // Load existing profile and fill in the form
        UserProfile p = UserProfile.load(this);

        etAge.setText(String.valueOf(p.getAge()));
        etWeight.setText(String.valueOf(p.getWeightKg()));
        etHeight.setText(String.valueOf(p.getHeightCm()));
        if (p.getBodyFatPercent() > 0) {
            etBodyFat.setText(String.valueOf(p.getBodyFatPercent()));
        }

        // Gender
        if ("female".equals(p.getGender())) {
            toggleGender.check(R.id.btnFemale);
        } else {
            toggleGender.check(R.id.btnMale);
        }

        // Activity
        switch (p.getActivityLevel()) {
            case 1: rgActivity.check(R.id.rbSedentary); break;
            case 2: rgActivity.check(R.id.rbLightly); break;
            case 3: rgActivity.check(R.id.rbModerate); break;
            case 4: rgActivity.check(R.id.rbVery); break;
            case 5: rgActivity.check(R.id.rbExtra); break;
        }

        // Goal
        switch (p.getGoal()) {
            case "cut": toggleGoal.check(R.id.btnCut); break;
            case "bulk": toggleGoal.check(R.id.btnBulk); break;
            default: toggleGoal.check(R.id.btnMaintain);
        }
    }

    private void initViews() {
        toggleGender   = findViewById(R.id.toggleGender);
        toggleGoal     = findViewById(R.id.toggleGoal);
        etAge          = findViewById(R.id.etAge);
        etWeight       = findViewById(R.id.etWeight);
        etHeight       = findViewById(R.id.etHeight);
        etBodyFat      = findViewById(R.id.etBodyFat);
        rgActivity     = findViewById(R.id.rgActivity);
        tvTargetPreview = findViewById(R.id.tvTargetPreview);
        tvTDEEInfo     = findViewById(R.id.tvTDEEInfo);
        cardPreview    = findViewById(R.id.cardPreview);
        btnSave        = findViewById(R.id.btnSave);

        // Defaults
        toggleGender.check(R.id.btnMale);
        toggleGoal.check(R.id.btnMaintain);
        rgActivity.check(R.id.rbSedentary);
    }

    private void setupListeners() {
        // Text change listeners for live preview
        TextWatcher recalcWatcher = new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                recalculate();
            }
        };

        etAge.addTextChangedListener(recalcWatcher);
        etWeight.addTextChangedListener(recalcWatcher);
        etHeight.addTextChangedListener(recalcWatcher);
        etBodyFat.addTextChangedListener(recalcWatcher);

        toggleGender.addOnButtonCheckedListener((g, id, checked) -> recalculate());
        toggleGoal.addOnButtonCheckedListener((g, id, checked) -> recalculate());
        rgActivity.setOnCheckedChangeListener((g, id) -> recalculate());

        btnSave.setOnClickListener(v -> saveProfile());
    }

    private void recalculate() {
        UserProfile profile = buildProfileFromInputs();
        if (profile == null) {
            cardPreview.setVisibility(View.GONE);
            return;
        }

        int target = profile.calculateTargetCalories();
        double tdee = profile.calculateTDEE();
        double bmr = profile.calculateBMR();

        cardPreview.setVisibility(View.VISIBLE);
        tvTargetPreview.setText(String.format(Locale.US, "%,d kcal/day", target));
        tvTDEEInfo.setText(String.format(Locale.US,
                "BMR: %.0f kcal  |  TDEE: %.0f kcal  |  %s",
                bmr, tdee, getGoalLabel()));
    }

    private UserProfile buildProfileFromInputs() {
        // Validate required fields
        String ageStr = etAge.getText().toString().trim();
        String weightStr = etWeight.getText().toString().trim();
        String heightStr = etHeight.getText().toString().trim();

        if (ageStr.isEmpty() || weightStr.isEmpty() || heightStr.isEmpty()) return null;

        int age;
        double weight, height;
        try {
            age = Integer.parseInt(ageStr);
            weight = Double.parseDouble(weightStr);
            height = Double.parseDouble(heightStr);
        } catch (NumberFormatException e) {
            return null;
        }
        if (age <= 0 || age > 120 || weight <= 0 || height <= 0) return null;

        UserProfile p = new UserProfile();
        p.setAge(age);
        p.setWeightKg(weight);
        p.setHeightCm(height);

        // Gender
        p.setGender(toggleGender.getCheckedButtonId() == R.id.btnFemale ? "female" : "male");

        // Activity level (1-5)
        p.setActivityLevel(getActivityLevel());

        // Body fat (optional)
        String bfStr = etBodyFat.getText().toString().trim();
        if (!bfStr.isEmpty()) {
            try {
                double bf = Double.parseDouble(bfStr);
                if (bf > 0 && bf < 80) p.setBodyFatPercent(bf);
            } catch (NumberFormatException ignored) {}
        }

        // Goal
        p.setGoal(getGoalValue());

        // Weekly rate (default to 0.5 kg/week for moderate pace)
        p.setWeeklyRateKg(0.5);

        return p;
    }

    private int getActivityLevel() {
        int id = rgActivity.getCheckedRadioButtonId();
        if (id == R.id.rbSedentary) return 1;
        if (id == R.id.rbLightly)   return 2;
        if (id == R.id.rbModerate)  return 3;
        if (id == R.id.rbVery)      return 4;
        if (id == R.id.rbExtra)     return 5;
        return 1;
    }

    private String getGoalValue() {
        int id = toggleGoal.getCheckedButtonId();
        if (id == R.id.btnCut)  return "cut";
        if (id == R.id.btnBulk) return "bulk";
        return "maintain";
    }

    private String getGoalLabel() {
        int id = toggleGoal.getCheckedButtonId();
        if (id == R.id.btnCut)  return "Cutting";
        if (id == R.id.btnBulk) return "Bulking";
        return "Maintaining";
    }

    private void saveProfile() {
        UserProfile profile = buildProfileFromInputs();
        if (profile == null) {
            Toast.makeText(this, "Please fill in age, weight, and height", Toast.LENGTH_SHORT).show();
            return;
        }

        profile.calculateTargetCalories();
        profile.save(this);

        Toast.makeText(this,
                String.format(Locale.US, "Target set: %,d kcal/day", profile.getTargetCalories()),
                Toast.LENGTH_LONG).show();

        if (isFirstRun) {
            // First run: go to main screen
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            // Edit mode: just close this activity
            finish();
        }
    }

    // ---- Minimal TextWatcher to reduce boilerplate ----
    private abstract static class SimpleTextWatcher implements TextWatcher {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
    }
}
