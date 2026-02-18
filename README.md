# CalMahAhh — AI Food Calorie & Protein Tracker

An Android app that captures or picks a food image, detects food items using
**Clarifai** (image recognition), retrieves nutrition data from **Edamam**
(food database), and lets you adjust portion sizes to see total calories and
protein at a glance.

---

## Prerequisites

| Tool | Version | Notes |
|------|---------|-------|
| **JDK** | 17+ | `java -version` to verify |
| **Android SDK** | API 34 (compileSdk) | Install via Android Studio SDK Manager or `sdkmanager` CLI |
| **Gradle** | 8.4 | Handled automatically by the Gradle Wrapper |

You also need the `ANDROID_HOME` (or `ANDROID_SDK_ROOT`) environment variable
pointing at your SDK directory. Create a `local.properties` file in the project
root if the build complains:

```properties
# Windows example:
sdk.dir=C\:\\Users\\YourName\\AppData\\Local\\Android\\Sdk
```

---

## Getting Free API Keys

### 1. Clarifai — Food Image Recognition

| | |
|---|---|
| **Sign up** | <https://clarifai.com/signup> |
| **Free tier** | Community plan — **1 000 operations / month** |
| **Get PAT** | Settings → Security → Personal Access Tokens → Create |
| **Stay free** | Do **not** enable billing; the community plan auto-limits |

Paste your PAT into `Constants.java`:
```java
public static final String CLARIFAI_PAT = "your_pat_here";
```

### 2A. ⭐ USDA FoodData Central API (nutrition data) — **Completely Free, No Credit Card**

Recommended free alternative to Edamam.

| | |
|---|---|
| **Sign up** | <https://fdc.nal.usda.gov/api-key/> |
| **Free tier** | Unlimited requests |
| **Get key** | Enter email → receive API key automatically |
| **No credit card required** | Yes ✓ |

**To use USDA instead:**
1. Get free API key from the link above.
2. Replace `EdamamService` with `USDAService` in the code.
3. The app will work identically.

### 2B. Edamam — Food Database API (nutrition data) — **Paid Plans Only**

Edamam no longer offers a completely free developer plan.

| | |
|---|---|
| **Sign up** | <https://developer.edamam.com/edamam-docs-food-database-api> |
| **Cheapest plan** | Basic Vision — **$14/month** (includes 30-day free trial, requires credit card) |
| **Get keys** | After subscription → Dashboard → Applications → Food Database |
| **Free component** | Food database API is bundled with paid plans |

**If you want completely free → use USDA FoodData Central instead.**

Paste your credentials into `Constants.java`:
```java
public static final String EDAMAM_APP_ID  = "your_app_id";
public static final String EDAMAM_APP_KEY = "your_app_key";
```

---

## Switching Between USDA (Free) and Edamam (Paid)

The app comes with **USDA as the default** (completely free).

### To use USDA (recommended):

1. Get your free API key from <https://fdc.nal.usda.gov/api-key/>
2. Paste it into [Constants.java](app/src/main/java/com/calmahahh/app/Constants.java):
   ```java
   public static final String USDA_API_KEY = "your_key_here";
   ```
3. The code in [MainActivity.java](app/src/main/java/com/calmahahh/app/MainActivity.java) already uses USDA.

### To switch to Edamam:

1. Subscribe to Basic Vision plan ($14/month): <https://developer.edamam.com/edamam-docs-food-database-api>
2. Copy your App ID and App Key into [Constants.java](app/src/main/java/com/calmahahh/app/Constants.java)
3. In [MainActivity.java](app/src/main/java/com/calmahahh/app/MainActivity.java), find the comment `// ===== Choose nutrition API =====` (around line 290)
4. Comment out the USDA lines and uncomment the Edamam lines
5. Rebuild: `.\gradlew.bat assembleDebug`

---

```bash
# Windows
gradlew.bat assembleDebug

# macOS / Linux
./gradlew assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`

### Release APK

```bash
gradlew.bat assembleRelease
```

> For a signed release build you need a keystore — see
> [Android signing docs](https://developer.android.com/studio/publish/app-signing).

### Install on connected device

```bash
gradlew.bat installDebug
```

---

## If the Gradle Wrapper is missing

If `gradlew.bat` and `gradle/wrapper/gradle-wrapper.jar` are not present,
generate them with an existing Gradle installation:

```bash
gradle wrapper --gradle-version 8.4
```

Or download the Gradle Wrapper JAR manually from
<https://services.gradle.org/distributions/gradle-8.4-bin.zip>, extract
`lib/gradle-wrapper.jar`, and place it in `gradle/wrapper/`.

---

## Project Structure

```
CalMahAhh/
├── build.gradle                        # root build script
├── settings.gradle
├── gradle.properties
├── gradle/wrapper/
│   └── gradle-wrapper.properties
├── app/
│   ├── build.gradle                    # app module build script
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── res/
│       │   ├── layout/
│       │   │   ├── activity_main.xml   # main screen layout
│       │   │   └── item_food.xml       # RecyclerView item
│       │   ├── values/                 # strings, colors, themes, dimens
│       │   ├── values-sw600dp/         # tablet dimension overrides
│       │   ├── drawable/               # shapes, vector icons
│       │   ├── mipmap-anydpi-v26/      # adaptive launcher icon
│       │   └── xml/file_paths.xml      # FileProvider config
│       └── java/com/calmahahh/app/
│           ├── Constants.java          # API keys (edit this!)
│           ├── MainActivity.java       # main activity
│           ├── adapter/
│           │   └── FoodAdapter.java    # RecyclerView adapter
│           ├── api/
│           │   ├── ApiClient.java      # Retrofit singletons
│           │   ├── ClarifaiService.java
│           │   ├── ClarifaiRequest.java
│           │   ├── ClarifaiResponse.java
│           │   ├── EdamamService.java
│           │   └── EdamamResponse.java
│           ├── model/
│           │   └── FoodItem.java       # food data model
│           └── util/
│               ├── ImageUtils.java     # image resize + Base64
│               ├── NetworkUtils.java   # connectivity check
│               └── NutritionCalculator.java
```

---

## How It Works

1. **Capture / Pick image** — Camera or gallery intent.
2. **Clarifai API** — Base64 image → top food labels (confidence ≥ 50 %).
3. **Edamam API** — Each label → calories & protein per 100 g.
4. **Display** — RecyclerView cards with editable portion (grams).
5. **Totals** — `calories = (caloriesPer100g × grams) / 100`.

### Error Handling

| Situation | Behaviour |
|-----------|-----------|
| No internet | Toast: *"No internet connection …"* |
| API failure | Toast with HTTP code |
| No food detected | In-line message: *"No food items detected …"* |
| Nutrition lookup fails for one item | Item silently skipped |

---

## Permissions

| Permission | Why |
|---|---|
| `INTERNET` | API calls to Clarifai + Edamam |
| `CAMERA` | Capture food photo |
| `ACCESS_NETWORK_STATE` | Check connectivity before API calls |

---

## Responsiveness

- **ConstraintLayout** for flexible positioning.
- **NestedScrollView** wraps the whole page for scrollable content.
- Dimensions use **dp** (spacing/margins) and **sp** (text).
- `values-sw600dp/dimens.xml` overrides for tablets (≥ 600 dp width).
- Images use `adjustViewBounds` + `centerCrop` / `centerInside`.

---

## Daily Free Limits — Stay Safe

| API | Free Tier Limit | Credit Card Required |
|-----|-----------------|---------------------|
| **Clarifai** | 1,000 ops/month | No ✓ |
| **USDA FoodData** | Unlimited | No ✓ |
| **Edamam** | ~200 req/min | Yes ($14/month after 30-day trial) |

---

## License

This project is provided as-is for educational purposes.
