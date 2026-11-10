# Repository instructions

## Releases

- Pushes to `main` automatically release the pushed commit through `.github/workflows/android.yml`: select the next patch version, test, build, verify the signed APK, push its tag, and publish the GitHub Release.
- Follow README.md under "Publish a release" for automatic releases and optional manual version tags.
- Keep release publication in the Android workflow, using the existing signing key. PRs and manual workflow runs must not publish releases.
- For a manual version tag, use a new, unused `v`-prefixed version and push it with authenticated Git. If tools cannot push tags, provide the exact commands.
- Before reporting success, verify that the workflow passed and the published release contains its versioned APK.
