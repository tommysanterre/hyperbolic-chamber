# Repository instructions

## Releases

- Pushes to `main` automatically release the pushed commit through `.github/workflows/android.yml`: select the next patch version, test, build, verify the signed APK, push its tag, and publish the GitHub Release.
- Follow README.md under "Publish a release" for automatic releases and optional manual version tags.
- Keep release publication in the Android workflow, using the existing signing key. PRs and manual workflow runs must not publish releases.
- For a manual version tag, use a new, unused `v`-prefixed version and push it with authenticated Git. If tools cannot push tags, provide the exact commands.
- Before reporting success, verify that the workflow passed and the published release contains its versioned APK.

## Personal trainer & tracking docs

- This repository serves as both the Android tracker app and the athlete's personal-training record.
- Before recommending, adapting, or reviewing workouts, exercise snacks, or app routines, consult:
  - `docs/equipment-inventory.md` (owned gear with setup/preference notes, plus wishlist gear)
  - `docs/training-plan.md` (goals, constraints, active 3 × 30-min program, daily snack loop, and current baselines)
  - `docs/training-log.md` (recent sessions, discomfort notes, exported history reviews, and coaching adjustments)
- Only prescribe active movements using **Owned** gear; keep **Wishlist** items clearly marked as future upgrades, and feel free to recommend new equipment anytime there is a genuinely good reason for a goal, progression bottleneck, or pain-free adaptation. Treat negative setup/preference notes in `docs/equipment-inventory.md` (e.g., disliked, removed, or uninstalled items) as **soft nos**, not hard bans—avoid defaulting to them, but feel free to suggest them again when they are a genuinely strong fit for a specific goal or adaptation (noting the trade-off).
- Whenever the user logs a workout, shares an exported history report, buys/retires equipment, reports pain/discomfort, or changes progression targets, update the corresponding files in `docs/` (`equipment-inventory.md`, `training-plan.md`, and/or `training-log.md`).
