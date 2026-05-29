# Changelog

## Unreleased - 2026-05-29
### Android (HalalyticsCompose)
- **Fix**: Resolved "Force Close" issue after Splash Screen by adding extensive logging and validating navigation routes.
- **Fix**: Hardened `MainActivity` lifecycle with a more robust `setContent` initialization and `isReady` state.
- **New**: Fully integrated **AI-Powered Product Comparison** with Head-to-Head analysis and "Better Choice" recommendations.
- **Improvement**: Implemented **Token Expiry Checking** in `SessionManager` and `AuthInterceptor` for better session handling.
- **Improvement**: Redesigned `ComparisonResultScreen` with a premium card-based UI and horizontal side-by-side analysis.
- **Improvement**: Added `isLoading` state and premium animations to `PrimaryButton` and `SecondaryButton`.
- **Improvement**: Added `GlobalCrashHandler` logging to capture fatal exceptions in Logcat with `HALALYTICS_CRASH` tag.
- **Improvement**: Synchronized `RoleHelper` home routes with `MainActivity` navigation graph.

### Web/Backend (Halalytics)
- **New**: Implemented **Health Encyclopedia (Kamus Kesehatan)** database migration and model.
- **New**: Created **Admin Panel for Health Encyclopedia** (CRUD) with full filtering and search capabilities.
- **New**: Added **Filament Resource for Product Fake Reports** to manage crowd-sourced safety data.
- **Database**: Added `family_id` to `medicine_reminders` for shared family medication schedules.
- **Improvement**: Migrated `HealthEncyclopediaController` API to use real database data with a fallback mechanism.
- **Improvement**: Aligned `ProductComparisonController` response with Android data models for seamless integration.
- **Seed**: Added `HealthEncyclopediaSeeder` with initial health information.

## Unreleased - 2026-05-19
- Fix: Hardened `SplashScreen` navigation to avoid invalid destinations and navigation loops.
- Fix: Normalised composable call sites in `MainActivity.kt` to remove problematic named-arg usages.
- Fix: Corrected `ApiConfig.getOpenBeautyFactsApiService()` to return `OpenBeautyFactsApiService`.
- Improvement: Added robust fallbacks in `ProductExternalViewModel` and `ProductRepository` to use OpenFoodFacts/OpenBeautyFacts when backend external bridge fails.
- Build: Verified Kotlin compilation and generated `app-debug.apk`.

## Notes
- Next steps: device/emulator verification (`adb` required), audit deprecation warnings and gradually migrate icons to `Icons.AutoMirrored` safely.
