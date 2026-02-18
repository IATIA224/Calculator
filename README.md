# CalMahAhh 🐕 — AI Food Tracker with Gemini Vision

A sleek Android app that identifies foods using **Google Gemini Vision AI**, estimates portions, calculates full macronutrient breakdowns (calories, protein, carbs, fat), and tracks your daily intake against a personalized calorie target based on TDEE calculations.

---

## Features

✅ **Gemini Vision AI** — Snap a photo of your food; AI identifies it and estimates weight  
✅ **TDEE Calculator** — First-run survey (age, weight, height, activity level, goal) calculates your daily calorie target  
✅ **Macro Tracking** — See calories, protein, carbs, fat per food item & daily totals  
✅ **Meal Logging** — Add scanned foods to Morning / Afternoon / Evening meals  
✅ **Daily Progress** — Visual progress bar shows consumed vs. target calories  
✅ **Editable Portions** — Adjust portion sizes and see totals update live  
✅ **Profile Management** — Edit your profile anytime with back button (no forced save)  
✅ **Dog Icon** — 🐕 CAPY.png mascot in app launcher

---

## Setup & Installation

### 1. Get Google Gemini API Key (Free)

1. Visit **[Google AI Studio](https://aistudio.google.com/apikey)**
2. Click **"Create API Key"**
3. Copy the key
4. Paste into `app/src/main/java/com/calmahahh/app/Constants.java`:
   ```java
   public static final String GEMINI_API_KEY = "your_key_here";
   ```

**Free tier limits:**
- 15 requests/minute
- 1 million tokens/day
- No credit card required ✓

### 2. Build & Install

**Requirements:**
- JDK 17+
- Android SDK (API 34+)
- Gradle 8.4+

**Build the APK:**
```bash
cd CalMahAhh
./gradlew assembleDebug
```

**Install on device:**
```bash
./gradlew installDebug
```

---

## Usage

### First Launch
1. **Survey Screen** — Enter your stats (gender, age, weight, height, activity level, goal)
2. View your calculated **daily calorie target**
3. Save → Go to home screen

### Home Screen
1. **Tap Camera or Gallery** — Pick a food image
2. **(Optional) Add context** — E.g., "200g tofu" to improve AI accuracy
3. **Tap "Analyze Food"** — Gemini AI identifies foods & estimates portions
4. **Adjust portions** — Edit grams; macros recalculate instantly
5. **Add to Meal** — Tap "+ Morning / + Afternoon / + Evening" to log the meal
6. **Track Progress** — Daily progress card shows consumed vs. target in real-time

### Edit Profile
- Tap **"Edit Profile"** on home screen
- Update your stats
- Tap **Save** to apply or **Back** to discard changes

---

## Project Structure

```
CalMahAhh/
├── app/src/main/java/com/calmahahh/app/
│   ├── MainActivity.java              # Main home screen, food scanning
│   ├── SurveyActivity.java            # TDEE survey (first-run & edit mode)
│   ├── Constants.java                 # API keys
│   ├── model/
│   │   ├── UserProfile.java           # TDEE calculation & persistence
│   │   ├── FoodItem.java              # Food data model
│   │   └── MealLog.java               # Daily meal tracking
│   ├── api/
│   │   ├── GeminiService.java         # Gemini API (Retrofit)
│   │   ├── GeminiRequest.java         # Request builder
│   │   └── GeminiResponse.java        # Response parser
│   ├── adapter/
│   │   └── FoodAdapter.java           # RecyclerView for food items
│   └── util/
│       ├── ImageUtils.java            # Image compression & base64
│       ├── NetworkUtils.java          # Network checks
│       └── NutritionCalculator.java   # Macro aggregation
├── app/src/main/res/
│   ├── layout/
│   │   ├── activity_main.xml          # Home screen
│   │   ├── activity_survey.xml        # Survey form
│   │   └── item_food.xml              # Food list item
│   └── values/
│       ├── strings.xml                # UI text
│       ├── colors.xml                 # Color palette
│       ├── dimens.xml                 # Dimensions
│       └── themes.xml                 # Material 3 theme
└── build.gradle                       # Dependencies (Retrofit, Gson, Material)
```

---

## TDEE Calculation

**BMR Formula:**
- **Mifflin-St Jeor** (default): Uses age, weight, height, gender
- **Katch-McArdle** (if body fat % provided): Uses lean body mass

**TDEE:** BMR × Activity Multiplier (1.2 to 1.9 based on activity level)

**Daily Target:**
- **Cut:** TDEE − (0.5 kg/week × 1100 kcal)
- **Maintain:** TDEE
- **Bulk:** TDEE + (0.5 kg/week × 1100 kcal)

---

## Dependencies

- **Retrofit 2** — REST API calls
- **Gson** — JSON parsing
- **Material Design 3** — UI components
- **Core Android** — Permissions, shared preferences

---

## APK Download

**Latest Release:** [CalMahAhh v1.0](../../releases)

Download `CalMahAhh-debug.apk` and install on your Android device (API 34+).

---

## License

Open source. Feel free to modify and share!

---

## Author

Built with ❤️ for food tracking.
