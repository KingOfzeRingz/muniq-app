# Web Deployment Setup

## Status: ⚠️ Blocked

GitHub Pages deployment infrastructure has been set up, but deployment is currently blocked by WASM compilation errors.

## What's Ready ✅

1. **GitHub Actions Workflow**: [`.github/workflows/deploy-web.yml`](file:///Users/maksimsigov/StudioProjects/Muniq/.github/workflows/deploy-web.yml)
   - Configured to build and deploy on push to `main`
   - Uses correct Gradle task: `:composeApp:composeCompatibilityBrowserDistribution`
   - Deploys to GitHub Pages automatically

2. **README Updated**: Added live demo link (will work once deployment succeeds)

3. **Repository**: `KingOfzeRingz/muniq-app`

## Blocker: WASM Compilation Errors ❌

The `wasmJs` target in [`build.gradle.kts`](file:///Users/maksimsigov/StudioProjects/Muniq/composeApp/build.gradle.kts) is currently commented out because [`MapComposable.wasmJs.kt`](file:///Users/maksimsigov/StudioProjects/Muniq/composeApp/src/wasmJsMain/kotlin/com/doubleu/muniq/platform/MapComposable.wasmJs.kt) has compilation errors:

```
e: Dynamic type is only supported in Kotlin JS.
e: Calls to 'js(code)' must be a single expression inside a top-level function body
e: Type 'Nothing' cannot be used as type of JS interop property
```

**Root Cause**: The WASM implementation uses `dynamic` types and `js()` calls which aren't compatible with Kotlin/Wasm. The Google Maps integration needs to be rewritten using WASM-compatible JS interop.

## Next Steps to Enable Deployment

### Option 1: Fix WASM Map Implementation (Recommended)
Rewrite `MapComposable.wasmJs.kt` to use Kotlin/Wasm compatible JS interop:
- Replace `dynamic` types with proper external interfaces
- Use `@JsModule` and external declarations instead of `js()` calls
- Follow Kotlin/Wasm JS interop guidelines

### Option 2: Disable Map for Web (Quick Fix)
Show a placeholder message on web instead of the map:
- Keep wasmJs target commented out for now
- Deploy without map functionality
- Add map support later

### Option 3: Use Different Web Framework
Consider using Kotlin/JS instead of Kotlin/Wasm for better Google Maps compatibility.

## How to Deploy (Once Fixed)

1. Uncomment `wasmJs` target in `build.gradle.kts` (lines 20-23)
2. Fix WASM compilation errors in `MapComposable.wasmJs.kt`
3. Test locally: `./gradlew :composeApp:composeCompatibilityBrowserDistribution`
4. Push to `main` branch
5. GitHub Actions will automatically build and deploy
6. Site will be live at: https://kingofzeringz.github.io/muniq-app/

## GitHub Pages Configuration Required

Before first deployment, enable GitHub Pages in repository settings:
1. Go to repository Settings → Pages
2. Source: "GitHub Actions"
3. Save

The workflow will handle the rest automatically.
