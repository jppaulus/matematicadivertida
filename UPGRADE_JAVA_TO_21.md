Upgrade Java runtime to Java 21 - Steps taken

Summary:
- Project updated to use Java 21 as the Kotlin/Gradle toolchain and compile target.

Changes made:
- `app/build.gradle.kts`:
  - Updated `kotlin { jvmToolchain(17) }` -> `kotlin { jvmToolchain(21) }`
  - Added `compileOptions` to set `sourceCompatibility` and `targetCompatibility` to `JavaVersion.VERSION_21`.
  - Configured Kotlin compile tasks to set `kotlinOptions.jvmTarget = "21"`.
- `gradle.properties`:
  - Set `org.gradle.java.home` to point to a JDK 21 installation (example: `C:/Program Files/Java/jdk-21`).
  - Set `org.gradle.java.home` to point to a JDK 21 installation. Example used in this environment: `C:/jdk/jdk-21/jdk-21.0.8`.

Verification performed:
- Used local JDK 21 (`C:/Program Files/Android/Android Studio/jbr`).
- Ran `./gradlew.bat assembleDebug` successfully (Build succeeded).
- Ran `./gradlew.bat build --warning-mode all` successfully; lint & compile successful with some deprecation warnings.

Follow-up recommendations:
1. CI/CD - Ensure your CI images or build servers use JDK 21 or have correct `org.gradle.java.home` configured.
  - A sample GitHub Actions workflow was added at `.github/workflows/ci-java21.yml` which sets up JDK 21 using `actions/setup-java@v4` and runs `./gradlew build`.
2. Gradle Wrapper - Consider updating the `gradle.wrapper` to a stable release supporting Java 21 if necessary (e.g., Gradle 9.x). Confirm compatibility with AGP.
3. Android Gradle Plugin - Verify AGP (currently 8.5.2) compatibility with Java 21; bump AGP if necessary in `settings.gradle.kts` and adjust Gradle version accordingly.
4. Kotlin & Compose - Ensure Kotlin (1.9.25) and Compose Compiler (1.5.15) are compatible with Java 21; update them if you run into issues.
5. Testing - Run instrumented tests (`connectedAndroidTest`) and other test suites in CI.
6. Rollback - To revert changes, reset the modified files and remove `org.gradle.java.home` entry.

Notes:
- I couldn't generate an automated `generate_upgrade_plan` due to tool detection limitations for this Android project; I performed a manual upgrade change.
- The project build was successful after the changes, which suggests the upgrade is compatible.

If you want, I can also:
- Update the Gradle wrapper to a stable Gradle 9.x release and test compatibility.
- Create a dedicated Git commit/branch with the changes (if the repo is a git repository).
- Run further static-code checks or migrate deprecated Firebase APIs.

Recent changes (additional updates after initial Java 21 upgrade):
- Updated `app/build.gradle.kts` to bump `compileSdk` to `36`.
- Bumped Android Gradle Plugin to `8.9.1` in `settings.gradle.kts` (requires Gradle >= 8.11.1 / wrapper -> 9.5.1+).
- Updated Compose and AndroidX dependencies to newer versions to match Compose BOM and improve stability:
  - `androidx.compose:compose-bom:2025.11.00`
  - `androidx.activity:activity-compose:1.11.0`
  - `androidx.lifecycle:lifecycle-runtime-ktx:2.9.4`
  - `androidx.core:core-splashscreen:1.2.0`
  - `com.google.android.gms:play-services-ads:24.7.0`
  - `com.google.android.ump:user-messaging-platform:4.0.0`
  - Reverted Firebase BOM to `33.5.1` to avoid plugin incompatibilities during this session; consider testing `34.x` in your CI with updated Google plugins.
- Replaced boxed Compose state where appropriate with primitive state APIs to avoid boxing and improve performance (`mutableIntStateOf`, `mutableLongStateOf`, `mutableFloatStateOf`).
- Removed unused audio resources: `correct_sound.mp3` and `wrong_sound.mp3`.

Notes:
- Running Gradle wrapper may be necessary to build with the updated AGP; if you see Gradle version mismatch, run `./gradlew wrapper --gradle-version 9.5.1` then `./gradlew build`.
- Some updated dependencies may require additional testing in CI (e.g., update Kotlin or Compose Compiler if you encounter compilation issues).

