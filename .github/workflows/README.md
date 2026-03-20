# CI/CD Workflows

This directory contains GitHub Actions workflows for building and testing Kotlin Multiplatform projects. Builds are dispatched by the Mismaiti backend via `workflow_dispatch`.

## Workflows

### Android Build (`build-android.yml`)
Builds Android application with:
- JDK 17 setup
- Gradle caching
- Unit tests (optional)
- Debug & Release APK generation
- Artifact upload
- Backend callback on completion

**Outputs:**
- `app-debug.apk`
- `app-release.apk`
- Test results (if enabled)

### iOS Build (`build-ios.yml`)
Builds iOS application with:
- Xcode 15.2
- CocoaPods dependency management
- Unit tests (optional)
- IPA generation
- Artifact upload
- Backend callback on completion

**Outputs:**
- `app-ios-debug.ipa`
- Test results (if enabled)

**Note:** Requires macOS runner (higher cost)

## How Builds Are Triggered

Builds are **not** triggered by push events. Instead:

1. Mismaiti backend generates code and pushes to the repository
2. Backend sends a Discord notification to stakeholders
3. A stakeholder calls `POST /api/v1/dispatch-build` with `session_id` and `platform` (android/ios)
4. Backend dispatches the appropriate workflow via GitHub API `workflow_dispatch`
5. On completion, the workflow calls back to `POST /api/v1/build-complete`
6. Backend uploads the build to Firebase App Distribution

### Workflow Dispatch Inputs

| Input | Required | Description |
|-------|----------|-------------|
| `session_id` | Yes | Mismaiti session ID for tracking |
| `callback_url` | Yes | Backend URL for build-complete callback |
| `callback_token` | Yes | Short-lived token for build-complete callback authentication |
| `enable_unit_tests` | No | Run unit tests (default: true) |

All inputs are provided by the backend at dispatch time — no repository secrets or variables needed for the callback.

## Artifacts

Build artifacts are retained for 30 days and can be downloaded from the GitHub Actions run page.

## Troubleshooting

### Android Build Fails
- Check Gradle version compatibility
- Verify JDK 17 is used
- Check for missing dependencies

### iOS Build Fails
- Verify Xcode version (15.2 required)
- Check CocoaPods installation
- Verify code signing configuration
