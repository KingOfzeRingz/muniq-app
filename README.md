# Muniq – Munich District Explorer

> Data-driven neighborhood discovery for Munich. Compose Multiplatform frontend + Kotlin/Spring backend + GCP deployment.

🌐 **Web demo**: https://kingofzeringz.github.io/muniq-app/

## ✨ Highlights

- Interactive Google Map that colors Munich districts green → red based on personalized scores.
- Drag-and-drop priority sheet to reorder metrics (rent, quietness, air, mobility, bike lanes, childcare, density).
- Detailed district modal with raw metrics, normalized scores, AI-style summaries, and toast interactions.
- Single shared UI codebase targeting **Android**, **iOS**, and **Web (JS)** with Compose Multiplatform.
- Kotlin/Spring Boot backend running on **Google Cloud Run** with Firestore storage + scripted ETL pipeline.

## 🧱 Repository Structure

```
.
├─ composeApp/        # Shared KMP module (UI, domain, platform expect/actuals)
│  ├─ src/commonMain  # Shared UI, domain logic, data models
│  ├─ src/androidMain # Android-specific actuals (Google Maps Compose, platform services)
│  ├─ src/iosMain     # iOS actuals (UIKitView + Google Maps SDK)
│  └─ src/jsMain      # Web actuals (Google Maps JS API)
├─ iosApp/            # Xcode workspace / Swift entry point for iOS
├─ muniq/
│  ├─ backend/        # Spring Boot service (REST API, Firestore integration, Docker/GCP deploy scripts)
│  ├─ data/           # Cleaned CSVs per metric
│  ├─ source_data/    # Raw open-data dumps from Munich
│  └─ scripts/        # Python ETL + normalization scripts (rent, noise, green index, etc.)
└─ build/gradle/...   # Standard Gradle wrappers/config
```

## 🏗 Architecture Overview

### Frontend (Compose Multiplatform)

- Shared feature screens (`MapScreen`, `PrioritySheet`, settings, sidebar) live in `composeApp/src/commonMain`.
- Map rendering is abstracted behind `expect fun MuniqMap(...)` with platform-specific actuals:
  - Android → Google Maps Compose + Maps Utils.
  - iOS → UIKitView embedding `GMSMapView`, toast delegate, polygon overlays.
  - Web → Google Maps JS API v3 with dynamic polygon creation and toasts.
- State + domain logic:
  - `rememberMunichMapContent` loads GeoJSON, merges backend scores, and computes score-driven colors.
  - Koin handles DI (`sharedModule`) with simple multiplatform-friendly `viewModel` bindings.
  - Drag-and-drop priorities rely on `sh.calvin.reorderable` (stubbed for non-Android targets).

### Backend (`muniq/backend`)

- Kotlin 2 + Spring Boot 3 + Spring Web.
- Firestore via Firebase Admin SDK for storing district payloads (`GreenRating` model).
- REST endpoints under `/api/green-ratings` for list/detail/create/update/delete (the app mostly consumes GET).
- Dockerfile + `deploy.sh` for Cloud Run deployments (GCP Artifact Registry + Cloud SQL/Firestore connectors).

### Data Pipeline (`muniq/scripts`)

- Python scripts ingest official Munich CSVs (noise, air quality, rent, bike lanes, PT stops, childcare, density).
- Each script normalizes metrics to 0–100 and writes aggregated CSVs under `muniq/data/`.
- `save_to_firestore.py` pushes combined payloads to Firestore so the mobile/web clients get up-to-date numbers.

## 🚀 Running the App

### Prerequisites

- JDK 17+
- Android Studio / IntelliJ IDEA with Kotlin Multiplatform plugin
- Xcode 15+ (for iOS build)
- Yarn/Node (for web build) – automatically managed via Gradle

### Android

```bash
./gradlew :composeApp:assembleDebug
# or run the “Android App” configuration in Android Studio
```

### iOS

```bash
# Generate/refresh CocoaPods + Compose frameworks
./gradlew :composeApp:podInstall

# Then open iosApp/iosApp.xcworkspace in Xcode and run on simulator/device
```

### Web (JS)

```bash
./gradlew :composeApp:jsBrowserDistribution
# build output in composeApp/build/dist/js/productionExecutable
```

### Backend

```bash
cd muniq/backend
./gradlew bootRun
# or docker build -t muniq-backend . && docker run ...
```

## 🔧 Environment & Config

- **API keys**: set Google Maps Android/iOS/Web keys via `local.properties`, `Config.xcconfig`, and the JS actual (`GOOGLE_MAPS_WEB_API_KEY`).
- **Backend credentials**: Firestore uses `GOOGLE_APPLICATION_CREDENTIALS`. Local dev expects a service-account JSON; Cloud Run uses Workload Identity.
- **GeoJSON**: stored in `composeApp/src/commonMain/composeResources/files/munich_districts.json` and loaded via compose resources.

## 🧪 Testing & Linting

- Kotlin Multiplatform tests: `./gradlew :composeApp:allTests`
- Spring Boot tests: `./gradlew test` inside `muniq/backend`
- ESLint/Prettier not needed (no manual JS), but `jsBrowserDistribution` validates bundling.

## 🗺 Roadmap

- Plug live apartment listings into the scoring layer.
- Add Berlin/Vienna datasets (pipeline already modularized).
- Community “vibe scores” + AI chat summarizer.

---

Questions? Open an issue or ping the maintainers. Happy hacking! 😊
