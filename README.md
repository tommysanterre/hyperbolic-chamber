# Hyperbolic Time Chamber

Hyperbolic Time Chamber is a tiny offline-first Android app for moving through a continuous rotation of movement snacks, named after the Hyperbolic Time Chamber in Dragon Ball Z.

## Rotation

1. Dead hang — 20–30 seconds
2. Deep squat hold — 20–30 seconds
3. Band pull-aparts — 10–15 reps
4. Three-point bridge — 3–5 reps per side
5. Scapular pull-ups — 3–5 reps
6. Hip-flexor stretch — 20–30 seconds per side
7. Squats — 10 reps
8. Bear plank hold — 10–15 seconds

Routine version 5 refocuses the daily snack loop on low-fatigue mobility, posture, and joint lubrication (using owned gear only, with gymnastic rings retired from active setup) to complement the 3 × 30-minute weekly strength and cardio program tracked in [docs/training-plan.md](docs/training-plan.md). See [docs/equipment-inventory.md](docs/equipment-inventory.md) and [docs/training-log.md](docs/training-log.md) for the personal-training inventory and session log.

Spread 1–2 snacks at a time across the day—primarily on non-workout days—and stop well short of failure. Complete the movement shown, record the amount, and Hyperbolic Time Chamber advances to the next movement. The queue persists across days with no daily completion requirement or limit.

A workout day runs from **4 AM to 4 AM in your local time**. The existing daily loop counter counts only after completing all eight exercises in order within that same workout day, returning the queue to the exercise where the loop started. For example, if your day starts at hip-flexor stretch, complete hip-flexor stretch and the following seven exercises to return to hip-flexor stretch and earn a loop. Partial progress toward this daily counter does not carry over at 4 AM; your position in the exercise queue does. Tap **0** to skip any movement, including timed holds. Skips are saved in history as **Skipped**, persist across restarts, and can be reversed with **Undo last**. A skip advances the queue but resets progress toward a completed loop; complete all eight exercises after it to earn the next loop.

Updating from earlier routines keeps saved history, original movement names and units, and previously earned loop totals. New records use the eight-movement routine (version 5); partial loops cannot span routine versions. Retired movements are no longer queued, and the queue resumes at the next appropriate movement in the new routine. No database reset is needed.

The current routine is version 5. Version 3 introduced Knee push-ups, Heel-dig bridges and Bear plank hold with separate movement IDs; version 4 replaced Chin-ups with Scapular pull-ups; version 5 brings back Dead hang, Deep squat hold and Hip-flexor stretch alongside Band pull-aparts while moving heavier strength work into the dedicated 3 × 30-minute workouts. Earlier records retain their original names, units and amounts.

Exercise references: [ACE knee push-up technique](https://www.acefitness.org/resources/everyone/exercise-library/13/bent-knee-push-up/), [heel-supported hamstring bridge](https://holisticphysiofitness.com.au/exercise/hamstring-bridge-with-heels-strength/), [University of Portsmouth plank variations](https://sport.port.ac.uk/news-events-and-blogs/6-plank-variations-to-strengthen-your-core), and [Crossover Health pull-up progressions](https://be-well.crossoverhealth.com/wp-content/uploads/sites/2/2022/08/Pull-up-Progression.pdf). The short targets above are this app's starting points.

## Export history for a difficulty review

Open **History → Export history**, then choose **Share file** or **Save file**. The app creates a UTF-8 `.txt` report that you can attach in a chat. Exporting works offline and includes the current routine, targets, available recording amounts and movement cues; a summary of the last 28 workout days; and the complete saved log with timestamps, amounts, skips and routine versions. Recent averages exclude skips and keep earlier exercise variations separate. Per-side amounts stay per side. Undone entries are absent because they were removed from history.

When sharing the report to adjust difficulty, also describe how hard each exercise felt, roughly how many more clean reps or seconds you could have done, any assistance or setup changes, and any discomfort. The app does not record those details, and reaching a target or recording limit alone does not establish readiness to progress. Workout dates use the export device's current time zone and the app's 4 AM boundary. Sharing opens Android's share sheet; saving opens the system file picker. History remains in the app after either action.

## Movement illustrations

The eight movements use four adult Saiyans, with two movements each:

| Character | Movements |
| --- | --- |
| Goku | Deep squat hold; bear plank hold |
| Vegeta | Hip-flexor stretch; squats |
| Gohan | Three-point bridge; scapular pull-ups |
| Trunks | Dead hang; band pull-aparts |

Every movement has base-form and Super Saiyan artwork. Completing one full loop within a single workout day unlocks Super Saiyan artwork for all eight movements. All figures have adult proportions. The artwork assets, references and generation prompts are documented in [routine-artwork.md](docs/routine-artwork.md).

## Launcher icon

The app uses a single Kami Tower inspired launcher icon. The package name and signing configuration retain their existing values so installed copies continue to update without losing saved history.

## Installable updates

Download the versioned APK, such as `Hyperbolic-Time-Chamber-v0.3.11.apk`, from the [latest release](https://github.com/tommysanterre/hyperbolic-chamber/releases/latest) for installs and updates; there is no need to open Actions or extract an artifact ZIP. Releases contain a signed APK built from the tagged commit. CI uses the same signing key and assigns an Android `versionCode` based on the workflow run number, so newer builds can update existing installations while keeping saved history. The workflow fails instead of publishing an unsigned update if signing is not configured.

On launch, the app checks the public GitHub Releases API and shows a dismissible prompt only when the latest published release has a newer version than the installed app. The prompt opens that release in a browser. If the check cannot reach GitHub, the app continues normally and tries again on the next launch. Installing the APK remains a manual step.

### Publish a release

Every push to `main`, including a merged pull request, automatically publishes a release of the pushed commit. The Android workflow selects the next patch version after the highest stable `vMAJOR.MINOR.PATCH` tag (for example, `v0.3.13` becomes `v0.3.14`), runs unit tests, builds and verifies the signed APK, then pushes the tag and publishes the release. Failed tests or signing checks prevent tagging and publication. Release runs queue sequentially, with room for up to 100 pending runs, so nearby pushes do not compete for the same version.

No extra access token or release secret is needed: the workflow uses its built-in `GITHUB_TOKEN` with write permission only in the release job. A tag pushed with that token does not start another workflow; the same run publishes the APK it already verified. Retrying a run reuses any stable version tag already attached to that commit and replaces that release's APK.

To choose a particular version, you can still push a new, unused `v`-prefixed tag with your authenticated Git connection. After updating your local `main`, choose an unused version (replace `v0.4.0` below if it is already taken):

```sh
git switch main
git pull --ff-only origin main
git tag v0.4.0
git push origin v0.4.0
```

To release a specific commit, use `git tag v0.4.0 <commit-sha>` instead. The tagged commit must contain the release workflow. If the available connection cannot push tags, report that limitation and provide the tag-and-push commands. Manual version tags are optional; merging to `main` already starts an automatic release.

Pushing the tag automatically runs unit tests, builds and verifies the signed APK, and publishes a [GitHub Release](https://github.com/tommysanterre/hyperbolic-chamber/releases) with generated release notes and a versioned APK such as `Hyperbolic-Time-Chamber-v0.3.11.apk` under **Assets**. The Android version name comes from the tag without the leading `v` (for example, `0.3.11`). Actions does the build automatically; downloading happens directly from Releases.

Builds on `main`, pushed version tags, and manual workflow runs also provide the **hyperbolic-update** Actions artifact. Manual workflow runs publish only that artifact; pull requests run tests without publishing an APK or release. Confirm the workflow passed and the release contains its versioned APK before treating a release as complete.

### Signing setup

The signing key and passwords are stored locally in the ignored `signing/` directory. **Back up `signing/snackloop-release.jks` and `signing/keystore.properties` somewhere secure outside this repository.** Losing the key means future APKs cannot update existing installations. Do not commit either file.

To enable the GitHub Actions artifact, add these four repository secrets under **Settings → Secrets and variables → Actions**:

| Secret | Value |
| --- | --- |
| `SNACKLOOP_KEYSTORE_BASE64` | Base64 encoding of `signing/snackloop-release.jks` |
| `SNACKLOOP_STORE_PASSWORD` | `storePassword` from `signing/keystore.properties` |
| `SNACKLOOP_KEY_ALIAS` | `keyAlias` from `signing/keystore.properties` |
| `SNACKLOOP_KEY_PASSWORD` | `keyPassword` from `signing/keystore.properties` |

On Windows, copy the Base64 value to the clipboard with:

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes((Resolve-Path signing/snackloop-release.jks))) | Set-Clipboard
```

Then run the **Android build** workflow on `main` and download `hyperbolic-update`. Extract the versioned APK from the artifact ZIP and install it. On an emulator or USB-connected device that already has a release signed with this key, `adb install -r Hyperbolic-Time-Chamber-v0.3.11.apk` updates it without clearing app data; use the actual downloaded filename for later versions. For local signed builds, run `gradle testDebugUnitTest assembleRelease` and use `app/build/outputs/apk/release/app-release.apk`. The local build uses the ignored signing files automatically. CI assigns a version code based on its run number.
