# Repository instructions

## Releases

- Release by tagging the merged commit and pushing a new, unused `v`-prefixed version tag with an authenticated Git connection.
- Follow the commands in README.md under "Publish a release".
- Let the existing `.github/workflows/android.yml` tag-push workflow test, build, verify the signed APK, and publish the GitHub Release.
- Do not create a temporary release branch, change workflow triggers, or manually publish a release as a substitute for pushing a tag.
- If the available tools cannot push tags, explain the limitation and provide the exact tag-and-push commands.
- Before reporting success, verify that the workflow passed and the published release contains its versioned APK.
