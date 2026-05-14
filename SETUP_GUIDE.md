# ☀️ Surya-Shakti Solar Monitor — Complete Setup Guide
## MindMatrix VTU Internship — Project #87

---

## WHAT THIS APP DOES
Surya-Shakti is a personal energy dashboard for solar-enabled homes. It lets users:
- Log daily solar generation and consumption
- See their "Green Energy Independence" score (0–100)
- Simulate generation based on weather (Sunny/Cloudy)
- View a 30-day savings report with bar chart
- Get notifications for Peak Sun and Over-Generation
- Read solar energy tips to become a Prosumer

---

## STEP 1 — PREREQUISITES (Install These First)

1. **Android Studio** (Hedgehog 2023.1.1 or newer)
   - Download: https://developer.android.com/studio
   - Install with default settings (includes Android SDK)

2. **JDK 17** — Android Studio installs this automatically

3. **Android SDK** — Android Studio installs this automatically
   - Minimum SDK: API 24 (Android 7.0)
   - Target SDK: API 34 (Android 14)

---

## STEP 2 — OPEN PROJECT IN ANDROID STUDIO

1. Extract the ZIP file you downloaded → you will get a folder called `SuryaShakti`
2. Open **Android Studio**
3. Click **"Open"** (NOT "New Project")
4. Navigate to the extracted `SuryaShakti` folder
5. Click **OK / Open**
6. Wait for Gradle sync to finish (watch the progress bar at the bottom)
   - First time may take 3–5 minutes (downloads dependencies)
   - You will see "Gradle sync finished" when done

---

## STEP 3 — FIX local.properties (IMPORTANT)

Android Studio needs to know where your Android SDK is.

1. In Android Studio, open `local.properties` (in the root of the project)
2. Replace the content with:
   ```
   sdk.dir=C\:\\Users\\YOUR_USERNAME\\AppData\\Local\\Android\\Sdk
   ```
   (Replace YOUR_USERNAME with your actual Windows username)

   On **Mac/Linux**:
   ```
   sdk.dir=/Users/YOUR_USERNAME/Library/Android/sdk
   ```

   **TIP:** In Android Studio → File → Project Structure → SDK Location — you can see the exact path.

---

## STEP 4 — SYNC GRADLE

1. After opening the project, if you see **"Gradle files have changed"**, click **"Sync Now"**
2. Or go to: **File → Sync Project with Gradle Files**
3. Wait for sync to complete — you'll see "BUILD SUCCESSFUL" in the Build output

---

## STEP 5 — SET UP AN EMULATOR (OR USE REAL PHONE)

### Option A: Android Emulator (Recommended for testing)
1. In Android Studio, click the **Device Manager** icon (phone icon) on the right sidebar
2. Click **"Create Device"**
3. Choose: **Pixel 6** (or any phone) → Next
4. Download **API 34 (Android 14)** system image → Next → Finish
5. Click the ▶ Play button to start the emulator

### Option B: Real Android Phone
1. On your phone → Settings → About Phone → tap **Build Number 7 times** (enables Developer Mode)
2. Go to Settings → Developer Options → Enable **USB Debugging**
3. Connect phone to PC via USB
4. Allow the connection when prompted on the phone
5. Your phone will appear in the device dropdown in Android Studio

---

## STEP 6 — RUN THE APP

1. Select your device/emulator from the dropdown (top center of Android Studio)
2. Click the **▶ Run button** (green triangle) OR press **Shift + F10**
3. The app will build and install automatically
4. The Surya-Shakti dashboard will open on your device

---

## STEP 7 — USING THE APP

### Dashboard (Main Screen)
- **Enter Generated kWh** — solar panels produced today
- **Enter Consumed kWh** — electricity used today
- **Enter Battery Level** — current battery % (0–100)
- **Select Weather** — Sunny / Partly Cloudy / Cloudy
- **Tap "Simulate Generation"** — auto-fills generated kWh based on weather
- **Tap "Save Today's Log"** — saves to database
- The circular ring updates to show Solar vs Grid ratio
- Green Energy Independence score updates automatically

### Generation Log Screen
- Tap **"📋 Log"** button on dashboard
- See all past days with generated, consumed, battery, weather
- ⚡ "Exporting to Grid" badge shows over-generation days

### Savings Report Screen
- Tap **"📊 Report"** button on dashboard
- See 30-day totals: Generated, Consumed, Over-Gen Days, Total Savings
- Bar chart shows last 7 days comparison

### Tips Screen
- Tap **"💡 Tips"** button on dashboard
- 7 detailed solar energy tips

### Notifications
- Tap **"🔔"** button — sends a Peak Sun notification
- Over-generation alert fires automatically when you save a log where Generated > Consumed

---

## STEP 8 — TEST THE FEATURES (Success Criteria Check)

| Requirement | How to Test |
|---|---|
| Net Savings calculated at ₹8/unit | Save a log with 5 kWh generated, 3 kWh consumed → savings = 2×8 = ₹16 |
| High-contrast Yellow/Black UI | Visible in bright sunlight conditions |
| Over-generation handled | Enter generated > consumed → see ⚡ badge and notification |
| Non-numeric input validation | Leave a field blank or type "abc" → see error message |
| Weather simulation | Select Cloudy → Simulate → lower kWh shown vs Sunny |
| 30-day bar chart | Add a few logs → open Report screen |
| Battery progress bar | Enter battery=75 → green bar shows 75% |

---

## PROJECT FILE STRUCTURE

```
SuryaShakti/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/com/suryashakti/solar/
│   │   │   ├── data/
│   │   │   │   ├── db/        → AppDatabase.java, EnergyDao.java
│   │   │   │   ├── model/     → EnergyLog.java
│   │   │   │   └── repository/→ EnergyRepository.java, EnergyViewModel.java
│   │   │   ├── ui/
│   │   │   │   ├── dashboard/ → MainActivity.java
│   │   │   │   ├── log/       → GenerationLogActivity.java
│   │   │   │   ├── report/    → SavingsReportActivity.java
│   │   │   │   └── tips/      → TipsActivity.java
│   │   │   └── utils/
│   │   │       ├── NotificationHelper.java
│   │   │       └── WeatherSimulator.java
│   │   └── res/
│   │       ├── layout/        → All XML layouts
│   │       ├── drawable/      → Progress bars, icons
│   │       └── values/        → colors, strings, themes
│   └── build.gradle
├── build.gradle
├── settings.gradle
└── gradle.properties
```

---

## COMMON ERRORS & FIXES

### ❌ "SDK location not found"
→ Update `local.properties` with your SDK path (see Step 3)

### ❌ "Gradle sync failed: Could not resolve dependency"
→ Check your internet connection. Gradle needs to download libraries.
→ Try: File → Invalidate Caches → Restart

### ❌ "Minimum SDK version too high"
→ Open `app/build.gradle` → change `minSdk 24` to `minSdk 21`

### ❌ App crashes on launch
→ In Android Studio → View → Tool Windows → Logcat
→ Look for red error lines and share them

### ❌ "Build Tools revision not installed"
→ Go to: Tools → SDK Manager → SDK Tools → Install "Android SDK Build-Tools 34"

---

## ARCHITECTURE OVERVIEW

```
User Input (MainActivity)
        ↓
EnergyViewModel (LiveData)
        ↓
EnergyRepository
        ↓
Room Database (SQLite)
        ↓
EnergyDao → EnergyLog Entity

Side effects:
  WeatherSimulator → generates kWh estimate
  NotificationHelper → sends system notifications
```

**Libraries Used:**
- **Room** — local SQLite database (persistent energy logs)
- **LiveData + ViewModel** — reactive UI updates
- **MPAndroidChart** — bar chart in report screen
- **Material Components** — styled input fields and buttons

---

## RATE CALCULATION
- Fixed rate: **₹8 per kWh** (can be changed in `EnergyLog.java` constructor)
- Net savings = |Generated − Consumed| × 8
- If Generated > Consumed → money earned (over-generation / export)
- If Generated < Consumed → money saved from solar vs full grid cost

---

Good luck with your internship! 🌟
