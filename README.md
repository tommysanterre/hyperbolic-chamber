# SnackLoop

SnackLoop is a tiny offline-first Android app for moving through a continuous rotation of movement snacks.

## Rotation

1. Push-ups — 5–10 reps
2. Squats — 10–15 reps
3. Deep squat hold — 30 seconds
4. Pull-ups — 2–5 full pull-ups
5. Dead hang — 30 seconds
6. Reverse lunges — 5 reps per side
7. Hip-flexor stretch — 30 seconds per side

Complete the movement shown, record the amount, and SnackLoop advances to the next movement. The queue persists across days. There is no skip action or daily limit.

## Build

With Gradle 8.13 installed, run:

```shell
gradle testDebugUnitTest assembleDebug assembleOptimized
```

The conventional debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`.
The R8-optimized, resource-shrunk APK is written to
`app/build/outputs/apk/optimized/app-optimized.apk`. It is non-debuggable but uses the
debug signing key, so it is suitable for direct testing rather than store distribution.

Pushes and pull requests to `main` install the pinned Gradle 8.13 release, run the tests,
build both APKs with GitHub Actions, and upload `SnackLoop-debug` and
`SnackLoop-optimized` artifacts.
