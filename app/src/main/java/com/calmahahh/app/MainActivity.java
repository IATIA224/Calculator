package com.calmahahh.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.calmahahh.app.adapter.FoodAdapter;
import com.calmahahh.app.api.ApiClient;
import com.calmahahh.app.api.GeminiRequest;
import com.calmahahh.app.api.GeminiResponse;
import com.calmahahh.app.model.FoodItem;
import com.calmahahh.app.model.MealLog;
import com.calmahahh.app.model.UserProfile;
import com.calmahahh.app.util.ImageUtils;
import com.calmahahh.app.util.NetworkUtils;
import com.calmahahh.app.util.NutritionCalculator;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Response;

/**
 * Main screen of CalMahAhh.
 *
 * Flow:
 * 1. First launch → SurveyActivity (TDEE setup).
 * 2. User captures/picks an image of food.
 * 3. User optionally types context (e.g. "200g tofu", "2 slices").
 * 4. Image + context sent to Google Gemini Vision AI.
 * 5. AI identifies foods, estimates portions, calculates macros.
 * 6. Results shown in RecyclerView; user can edit portion sizes.
 * 7. User adds scanned foods to Morning / Afternoon / Evening meal.
 * 8. Daily progress bar tracks consumed vs target calories.
 */
public class MainActivity extends AppCompatActivity implements FoodAdapter.OnPortionChangedListener {

    // --- UI ---
    private ImageView imagePreview;
    private MaterialButton btnCamera, btnGallery, btnAnalyze;
    private MaterialButton btnAddMorning, btnAddAfternoon, btnAddEvening;
    private EditText etFoodContext;
    private RecyclerView recyclerFood;
    private View loadingOverlay, layoutMealButtons;
    private TextView tvTotalCalories, tvTotalProtein, tvTotalCarbs, tvTotalFat, tvNoResults;
    private TextView tvDailyTarget, tvConsumed, tvRemaining;
    private TextView tvMorningCal, tvAfternoonCal, tvEveningCal;
    private TextView tvEditProfile;
    private ProgressBar progressDaily;

    // --- Data ---
    private FoodAdapter foodAdapter;
    private final List<FoodItem> foodItems = new ArrayList<>();
    private Bitmap capturedBitmap;
    private Uri photoUri;
    private final Gson gson = new Gson();
    private UserProfile userProfile;

    // --- Threading ---
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // --- Activity-result launchers ---
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<String> permissionLauncher;

    // --- Gemini prompt ---
    private static final String GEMINI_PROMPT =
            "You are a professional nutritionist AI. Analyze this food image carefully.\n\n" +
            "INSTRUCTIONS:\n" +
            "1. Identify ALL food items visible in the image.\n" +
            "2. Estimate the portion size in grams for each item based on visual cues.\n" +
            "3. Calculate nutrition per 100g AND for the estimated portion.\n" +
            "4. If the user provides additional context below, use it to improve your estimates.\n\n" +
            "USER CONTEXT: %s\n\n" +
            "Return ONLY a JSON object with this EXACT structure (no markdown, no explanation):\n" +
            "{\n" +
            "  \"foods\": [\n" +
            "    {\n" +
            "      \"name\": \"Food name\",\n" +
            "      \"estimated_grams\": 150,\n" +
            "      \"calories_per_100g\": 200,\n" +
            "      \"protein_per_100g\": 10.5,\n" +
            "      \"carbs_per_100g\": 25.0,\n" +
            "      \"fat_per_100g\": 8.0,\n" +
            "      \"confidence\": 0.92\n" +
            "    }\n" +
            "  ]\n" +
            "}\n\n" +
            "RULES:\n" +
            "- confidence is 0.0 to 1.0 (how sure you are about the identification)\n" +
            "- estimated_grams should reflect what you SEE in the image\n" +
            "- All nutrition values are per 100 grams of the food\n" +
            "- If you cannot identify a food, still include it with your best guess\n" +
            "- Return valid JSON only, no extra text";

    // ---------------------------------------------------------------
    // Lifecycle
    // ---------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Redirect to survey if not completed
        if (!UserProfile.isSurveyCompleted(this)) {
            startActivity(new Intent(this, SurveyActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);
        userProfile = UserProfile.load(this);

        initViews();
        initLaunchers();
        setupRecyclerView();
        setupClickListeners();
        refreshDailyProgress();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh in case user updated profile or just returned
        if (userProfile != null) {
            userProfile = UserProfile.load(this);
            refreshDailyProgress();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }

    // ---------------------------------------------------------------
    // Initialisation helpers
    // ---------------------------------------------------------------

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        imagePreview    = findViewById(R.id.imagePreview);
        btnCamera       = findViewById(R.id.btnCamera);
        btnGallery      = findViewById(R.id.btnGallery);
        btnAnalyze      = findViewById(R.id.btnAnalyze);
        etFoodContext    = findViewById(R.id.etFoodContext);
        recyclerFood    = findViewById(R.id.recyclerFood);
        loadingOverlay  = findViewById(R.id.loadingOverlay);
        tvTotalCalories = findViewById(R.id.tvTotalCalories);
        tvTotalProtein  = findViewById(R.id.tvTotalProtein);
        tvTotalCarbs    = findViewById(R.id.tvTotalCarbs);
        tvTotalFat      = findViewById(R.id.tvTotalFat);
        tvNoResults     = findViewById(R.id.tvNoResults);

        // Daily progress views
        tvDailyTarget   = findViewById(R.id.tvDailyTarget);
        tvConsumed      = findViewById(R.id.tvConsumed);
        tvRemaining     = findViewById(R.id.tvRemaining);
        tvMorningCal    = findViewById(R.id.tvMorningCal);
        tvAfternoonCal  = findViewById(R.id.tvAfternoonCal);
        tvEveningCal    = findViewById(R.id.tvEveningCal);
        tvEditProfile   = findViewById(R.id.tvEditProfile);
        progressDaily   = findViewById(R.id.progressDaily);

        // Meal buttons
        layoutMealButtons = findViewById(R.id.layoutMealButtons);
        btnAddMorning    = findViewById(R.id.btnAddMorning);
        btnAddAfternoon  = findViewById(R.id.btnAddAfternoon);
        btnAddEvening    = findViewById(R.id.btnAddEvening);

        btnAnalyze.setEnabled(false);
    }

    private void initLaunchers() {
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && photoUri != null) {
                        try {
                            capturedBitmap = ImageUtils.loadAndResizeBitmap(this, photoUri, 1024);
                            imagePreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            imagePreview.setImageBitmap(capturedBitmap);
                            btnAnalyze.setEnabled(true);
                            clearResults();
                        } catch (IOException e) {
                            showError("Failed to load captured image");
                        }
                    }
                }
        );

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            try {
                                capturedBitmap = ImageUtils.loadAndResizeBitmap(this, uri, 1024);
                                imagePreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
                                imagePreview.setImageBitmap(capturedBitmap);
                                btnAnalyze.setEnabled(true);
                                clearResults();
                            } catch (IOException e) {
                                showError("Failed to load selected image");
                            }
                        }
                    }
                }
        );

        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (granted) {
                        launchCamera();
                    } else {
                        showError("Camera permission is required to take photos");
                    }
                }
        );
    }

    private void setupRecyclerView() {
        foodAdapter = new FoodAdapter(foodItems, this);
        recyclerFood.setLayoutManager(new LinearLayoutManager(this));
        recyclerFood.setAdapter(foodAdapter);
    }

    private void setupClickListeners() {
        btnCamera.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                launchCamera();
            } else {
                permissionLauncher.launch(Manifest.permission.CAMERA);
            }
        });

        btnGallery.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            galleryLauncher.launch(intent);
        });

        btnAnalyze.setOnClickListener(v -> analyzeImage());

        // Meal buttons
        btnAddMorning.setOnClickListener(v -> addMealAndRefresh(MealLog.MEAL_MORNING));
        btnAddAfternoon.setOnClickListener(v -> addMealAndRefresh(MealLog.MEAL_AFTERNOON));
        btnAddEvening.setOnClickListener(v -> addMealAndRefresh(MealLog.MEAL_EVENING));

        // Edit profile
        tvEditProfile.setOnClickListener(v -> {
            // Open survey to edit profile (don't clear existing data)
            startActivity(new Intent(this, SurveyActivity.class));
            // Don't finish, so user can come back with back button
        });
    }

    private void addMealAndRefresh(int mealType) {
        if (foodItems.isEmpty()) {
            showError("Scan food first before adding a meal");
            return;
        }
        MealLog.addMeal(this, mealType, foodItems);
        String name = MealLog.MEAL_NAMES[mealType];
        Toast.makeText(this, "Added to " + name + "!", Toast.LENGTH_SHORT).show();
        refreshDailyProgress();
    }

    // ---------------------------------------------------------------
    // Camera helpers
    // ---------------------------------------------------------------

    private void launchCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            File photoFile = createImageFile();
            photoUri = FileProvider.getUriForFile(this,
                    getPackageName() + ".fileprovider", photoFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            cameraLauncher.launch(intent);
        } catch (IOException e) {
            showError("Could not create image file");
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        return File.createTempFile("FOOD_" + timeStamp + "_", ".jpg", getCacheDir());
    }

    // ---------------------------------------------------------------
    // Gemini Vision AI analysis
    // ---------------------------------------------------------------

    private void analyzeImage() {
        if (capturedBitmap == null) {
            showError("Please capture or select an image first");
            return;
        }
        if (!NetworkUtils.isNetworkAvailable(this)) {
            showError("No internet connection. Please check your network and try again.");
            return;
        }

        String apiKey = Constants.GEMINI_API_KEY;
        if (apiKey.equals("YOUR_GEMINI_API_KEY_HERE") || apiKey.isEmpty()) {
            showError("Please set your Gemini API key in Constants.java");
            return;
        }

        showLoading(true);
        clearResults();

        // Get optional user context
        String userContext = etFoodContext.getText().toString().trim();
        if (userContext.isEmpty()) {
            userContext = "No additional context provided. Estimate everything from the image.";
        }

        final String prompt = String.format(GEMINI_PROMPT, userContext);

        executor.execute(() -> {
            try {
                // 1. Encode image to base64
                String base64 = ImageUtils.bitmapToBase64(capturedBitmap, 80);

                // 2. Send to Gemini Vision AI
                GeminiRequest request = GeminiRequest.create(prompt, base64);
                Response<GeminiResponse> response = ApiClient.getGeminiService()
                        .generateContent(apiKey, request)
                        .execute();

                if (!response.isSuccessful() || response.body() == null) {
                    String errorMsg = "Gemini API error (HTTP " + response.code() + ")";
                    if (response.code() == 400) {
                        errorMsg = "Invalid request format. Server rejected the request.";
                    } else if (response.code() == 401 || response.code() == 403) {
                        errorMsg = "Invalid API key. Check your Gemini API key in Constants.java";
                    } else if (response.code() == 404) {
                        errorMsg = "API endpoint not found. Gemini API might be unavailable.";
                    } else if (response.code() == 429) {
                        errorMsg = "Rate limit reached (15 req/min). Wait a moment and try again.";
                    } else if (response.code() == 500) {
                        errorMsg = "Gemini server error. Try again in a moment.";
                    }
                    
                    // Log the error response for debugging
                    try {
                        if (response.errorBody() != null) {
                            String error = response.errorBody().string();
                            android.util.Log.e("GeminiAPI", "Error: " + error);
                        }
                    } catch (Exception ignored) {}
                    
                    postError(errorMsg);
                    return;
                }

                // 3. Parse the AI response
                String jsonText = response.body().getText();
                if (jsonText == null || jsonText.isEmpty()) {
                    postError("AI returned empty response. Try a clearer photo.");
                    return;
                }

                List<FoodItem> detected = parseGeminiResponse(jsonText);

                // 4. Deliver results to UI
                mainHandler.post(() -> {
                    showLoading(false);
                    if (detected.isEmpty()) {
                        tvNoResults.setVisibility(View.VISIBLE);
                        tvNoResults.setText("No food items detected. Try a clearer photo.");
                        layoutMealButtons.setVisibility(View.GONE);
                    } else {
                        foodItems.clear();
                        foodItems.addAll(detected);
                        foodAdapter.notifyDataSetChanged();
                        updateTotals();
                        layoutMealButtons.setVisibility(View.VISIBLE);
                    }
                });

            } catch (IOException e) {
                postError("Network error: " + e.getMessage());
            } catch (Exception e) {
                postError("Error: " + e.getMessage());
            }
        });
    }

    /**
     * Parses the JSON response from Gemini into FoodItem objects.
     * Expected format: { "foods": [ { name, estimated_grams, calories_per_100g, ... } ] }
     */
    @SuppressWarnings("unchecked")
    private List<FoodItem> parseGeminiResponse(String json) {
        List<FoodItem> items = new ArrayList<>();
        try {
            // Parse the outer object
            Type mapType = new TypeToken<Map<String, Object>>() {}.getType();
            Map<String, Object> root = gson.fromJson(json, mapType);

            Object foodsObj = root.get("foods");
            if (foodsObj == null) return items;

            // Re-serialize and parse the foods array
            String foodsJson = gson.toJson(foodsObj);
            Type listType = new TypeToken<List<Map<String, Object>>>() {}.getType();
            List<Map<String, Object>> foods = gson.fromJson(foodsJson, listType);

            for (Map<String, Object> f : foods) {
                String name = getStringVal(f, "name", "Unknown food");
                double grams = getDoubleVal(f, "estimated_grams", 100);
                double calPer100 = getDoubleVal(f, "calories_per_100g", 0);
                double proPer100 = getDoubleVal(f, "protein_per_100g", 0);
                double carbPer100 = getDoubleVal(f, "carbs_per_100g", 0);
                double fatPer100 = getDoubleVal(f, "fat_per_100g", 0);
                double confidence = getDoubleVal(f, "confidence", 0.5);

                items.add(new FoodItem(name, calPer100, proPer100, carbPer100, fatPer100,
                        grams, confidence));
            }
        } catch (Exception e) {
            // If parsing fails, return empty list
            e.printStackTrace();
        }
        return items;
    }

    private String getStringVal(Map<String, Object> map, String key, String defaultVal) {
        Object val = map.get(key);
        return val != null ? val.toString() : defaultVal;
    }

    private double getDoubleVal(Map<String, Object> map, String key, double defaultVal) {
        Object val = map.get(key);
        if (val instanceof Number) return ((Number) val).doubleValue();
        if (val instanceof String) {
            try { return Double.parseDouble((String) val); } catch (Exception ignored) {}
        }
        return defaultVal;
    }

    // ---------------------------------------------------------------
    // Adapter callback
    // ---------------------------------------------------------------

    @Override
    public void onPortionChanged() {
        updateTotals();
    }

    // ---------------------------------------------------------------
    // UI helpers
    // ---------------------------------------------------------------

    private void updateTotals() {
        double cal = NutritionCalculator.calculateTotalCalories(foodItems);
        double pro = NutritionCalculator.calculateTotalProtein(foodItems);
        double carb = NutritionCalculator.calculateTotalCarbs(foodItems);
        double fat = NutritionCalculator.calculateTotalFat(foodItems);
        tvTotalCalories.setText(String.format(Locale.US, "%.0f kcal", cal));
        tvTotalProtein.setText(String.format(Locale.US, "%.1f g", pro));
        tvTotalCarbs.setText(String.format(Locale.US, "%.1f g", carb));
        tvTotalFat.setText(String.format(Locale.US, "%.1f g", fat));
    }

    private void showLoading(boolean show) {
        loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        btnCamera.setEnabled(!show);
        btnGallery.setEnabled(!show);
        btnAnalyze.setEnabled(!show && capturedBitmap != null);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void postError(String message) {
        mainHandler.post(() -> {
            showLoading(false);
            showError(message);
        });
    }

    private void clearResults() {
        foodItems.clear();
        foodAdapter.notifyDataSetChanged();
        tvNoResults.setVisibility(View.GONE);
        tvTotalCalories.setText("0 kcal");
        tvTotalProtein.setText("0 g");
        tvTotalCarbs.setText("0 g");
        tvTotalFat.setText("0 g");
        layoutMealButtons.setVisibility(View.GONE);
    }

    // ---------------------------------------------------------------
    // Daily progress tracking
    // ---------------------------------------------------------------

    private void refreshDailyProgress() {
        if (userProfile == null) return;

        int target = userProfile.getTargetCalories();
        MealLog.DailyLog log = MealLog.loadToday(this);
        double consumed = log.getTotalCalories();
        double remaining = Math.max(0, target - consumed);

        tvDailyTarget.setText(String.format(Locale.US, "%,d kcal", target));
        tvConsumed.setText(String.format(Locale.US, "Consumed: %.0f kcal", consumed));
        tvRemaining.setText(String.format(Locale.US, "Remaining: %.0f kcal", remaining));

        // Progress bar (cap at 100%)
        int percent = target > 0 ? (int) Math.min(100, (consumed / target) * 100) : 0;
        progressDaily.setProgress(percent);

        // Per-meal breakdown
        tvMorningCal.setText(String.format(Locale.US, "%.0f", log.getMealCalories("Morning")));
        tvAfternoonCal.setText(String.format(Locale.US, "%.0f", log.getMealCalories("Afternoon")));
        tvEveningCal.setText(String.format(Locale.US, "%.0f", log.getMealCalories("Evening")));

        // Color the progress bar: green if under, orange if near, red if over
        int color;
        if (percent > 100) {
            color = 0xFFF44336; // red
        } else if (percent > 90) {
            color = 0xFFFF9800; // orange
        } else {
            color = 0xFF4CAF50; // green
        }
        progressDaily.getProgressDrawable().setColorFilter(
                color, android.graphics.PorterDuff.Mode.SRC_IN);
    }
}
