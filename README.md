# Hyperbolic Time Chamber

Hyperbolic Time Chamber is a tiny offline-first Android app for moving through a continuous rotation of movement snacks, named after the Hyperbolic Time Chamber in Dragon Ball Z.

## Rotation

1. Knee push-ups — 3–8 reps
2. Squats — 10–15 reps
3. Three-point bridge — 3–5 reps per side
4. Chin-ups — 1–2 reps
5. Supported single-leg Romanian deadlift — 5–8 reps per side
6. Tuck L-sit — 5–15 seconds
7. Reverse lunges — 5 reps per side
8. Ring rows — 5–10 reps
9. Heel-dig bridges — 5–8 reps
10. Bear plank hold — 5–15 seconds

These variations keep setup quick and match the current starting level. Heel-dig bridges use the floor, so the rings can stay at rowing height. Bear planks use bent knees under the hips and a level back, avoiding the pike A-frame; they provide shoulder support and core practice rather than an overhead press. Knee push-ups keep a straight line from shoulders to knees. Targets are goals: the three adapted movements let you record from 1 rep or 1 second, so stop before form breaks and record the amount actually completed.

Complete the movement shown, record the amount, and Hyperbolic Time Chamber advances to the next movement. The queue persists across days: at 4–6 movements per day, work through the routine over roughly 2 days and resume wherever you stopped. There is no daily completion requirement or limit. Dead hangs, hip-flexor stretches and calf raises can be done as optional extras outside the tracked rotation.

A workout day runs from **4 AM to 4 AM in your local time**. The existing daily loop counter counts only after completing all ten exercises in order within that same workout day, returning the queue to the exercise where the loop started. For example, if your day starts at ring rows, complete ring rows and the following nine exercises to return to ring rows and earn a loop. Partial progress toward this daily counter does not carry over at 4 AM; your position in the exercise queue does. Tap **0** to skip any movement, including timed holds. Skips are saved in history as **Skipped**, persist across restarts, and can be reversed with **Undo last**. A skip advances the queue but resets progress toward a completed loop; complete all ten exercises after it to earn the next loop.

Updating from the previous eight-movement routine keeps saved history, its original movement names and units, and previously earned loop totals. New records use the ten-movement routine; a partial old loop cannot be combined with the new routine to earn a loop. Retired movements are no longer queued. The queue resumes after the last saved movement, using the next appropriate movement in the new routine.

The adapted routine is version 3. Records from version 2 retain Push-ups, Ring hamstring curls and Pike push-ups as their original names and rep units. New records use separate movement IDs for Knee push-ups, Heel-dig bridges and Bear plank hold. Completed loops from both earlier routines remain counted, and the queue resumes at the corresponding next slot; partial loops cannot span routine versions. No database reset is needed.

Exercise references: [ACE knee push-up technique](https://www.acefitness.org/resources/everyone/exercise-library/13/bent-knee-push-up/), [heel-supported hamstring bridge](https://holisticphysiofitness.com.au/exercise/hamstring-bridge-with-heels-strength/), and [University of Portsmouth plank variations](https://sport.port.ac.uk/news-events-and-blogs/6-plank-variations-to-strengthen-your-core). The short targets above are this app's starting points.

## Movement illustrations

The ten movements use four adult Saiyans, with character totals differing by at most one:

| Character | Movements |
| --- | --- |
| Goku | Knee push-ups; supported single-leg Romanian deadlift; bear plank hold |
| Vegeta | Squats; tuck L-sit; reverse lunges |
| Gohan | Three-point bridge; chin-ups |
| Trunks | Ring rows; heel-dig bridges |

Every movement has base-form and Super Saiyan artwork. The existing transformation unlock after two completed loops applies to all ten movements. Goku and Vegeta demonstrate three movements each; Gohan and adult Future Trunks demonstrate two each. All figures have adult proportions. The artwork assets, references and generation prompts are documented in [routine-artwork.md](docs/routine-artwork.md).

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
