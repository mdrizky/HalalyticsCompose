# Changelog

## Unreleased - 2026-05-19
- Fix: Hardened `SplashScreen` navigation to avoid invalid destinations and navigation loops.
- Fix: Normalised composable call sites in `MainActivity.kt` to remove problematic named-arg usages.
- Fix: Corrected `ApiConfig.getOpenBeautyFactsApiService()` to return `OpenBeautyFactsApiService`.
- Improvement: Added robust fallbacks in `ProductExternalViewModel` and `ProductRepository` to use OpenFoodFacts/OpenBeautyFacts when backend external bridge fails.
- Build: Verified Kotlin compilation and generated `app-debug.apk`.

## Notes
- Next steps: device/emulator verification (`adb` required), audit deprecation warnings and gradually migrate icons to `Icons.AutoMirrored` safely.
