# Hyperbolic Chamber

Hyperbolic Chamber is a tiny offline-first Android app for moving through a continuous rotation of movement snacks, named after the hyperbolic training chamber.

## Rotation

1. Push-ups — 5–10 reps
2. Squats — 10–15 reps
3. Deep squat hold — 30 seconds
4. Chin-ups — 1–2 reps
5. Dead hang — 30 seconds
6. Reverse lunges — 5 reps per side
7. Ring rows — 5–10 reps
8. Hip-flexor stretch — 30 seconds per side

Complete the movement shown, record the amount, and Hyperbolic Chamber advances to the next movement. The queue persists across days. There is no skip action or daily limit.

## Launcher icon

The app uses a single Kami Tower inspired launcher icon. The package name and signing configuration retain their existing values so installed copies continue to update without losing saved history.

## Installable updates

Download `app-release.apk` from the [latest release](https://github.com/tommysanterre/SnackLoop/releases/latest) for installs and updates; there is no need to open Actions or extract an artifact ZIP. Releases contain a signed APK built from the tagged commit. CI uses the same signing key and assigns an Android `versionCode` based on the workflow run number, so newer builds can update existing installations while keeping saved history. The workflow fails instead of publishing an unsigned update if signing is not configured.

### Publish a release

After committing and pushing these workflow changes, tag the commit you want to release and push the tag:

```sh
git tag v0.3.0
git push origin v0.3.0
```

To release a specific commit, use `git tag v0.3.0 <commit-sha>` instead. The tagged commit must contain the release workflow. Use a new `v`-prefixed version tag for each release.

Pushing the tag automatically runs unit tests, builds and verifies the signed APK, and publishes a [GitHub Release](https://github.com/tommysanterre/SnackLoop/releases) with generated release notes and `app-release.apk` under **Assets**. The Android version name comes from the tag without the leading `v` (for example, `0.3.0`). Actions does the build automatically; downloading happens directly from Releases. Private repositories still require repository access.

Builds on `main` and manual workflow runs continue to provide the **hyperbolic-update** Actions artifact without publishing a release.

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

Then run the **Android build** workflow on `main` and download `hyperbolic-update`. Extract `app-release.apk` from the artifact ZIP and install it. On an emulator or USB-connected device that already has a release signed with this key, `adb install -r app-release.apk` updates it without clearing app data. For local signed builds, run `gradle testDebugUnitTest assembleRelease` and use `app/build/outputs/apk/release/app-release.apk`. The local build uses the ignored signing files automatically. CI assigns a version code based on its run number.

**One-time transition:** Old `SnackLoop-debug` APKs were signed with temporary debug keys. Android cannot install the new release APK over an app signed by a different key. Moving from an old debug APK to the first signed release may require one uninstall, which can remove saved history. After that, install only signed release APKs from this key; later versions update in place. A local debug APK is for development and is not an update to the release APK.
