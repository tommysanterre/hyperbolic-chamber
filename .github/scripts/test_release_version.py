import unittest

from release_version import select_release_tag


class ReleaseVersionTest(unittest.TestCase):
    def test_increments_latest_patch_numerically(self):
        self.assertEqual(select_release_tag(["v0.3.9", "v0.3.13"], []), "v0.3.14")

    def test_respects_new_major_and_minor_versions(self):
        self.assertEqual(select_release_tag(["v0.9.99", "v1.0.0", "v1.2.0"], []), "v1.2.1")

    def test_ignores_prereleases_and_unrelated_tags(self):
        tags = ["v0.3.13", "v9.0.0-rc.1", "nightly", "v01.0.0", "1.0.0"]
        self.assertEqual(select_release_tag(tags, []), "v0.3.14")

    def test_starts_at_first_patch_without_stable_tags(self):
        self.assertEqual(select_release_tag([], []), "v0.0.1")

    def test_reuses_commit_tag_even_after_newer_releases(self):
        self.assertEqual(select_release_tag(["v0.3.13", "v0.3.14"], ["v0.3.13"]), "v0.3.13")

    def test_reuses_highest_stable_tag_on_commit(self):
        commit_tags = ["v0.3.9", "v0.3.13", "v9.0.0-rc.1"]
        self.assertEqual(select_release_tag(commit_tags, commit_tags), "v0.3.13")

    def test_prerelease_on_commit_does_not_prevent_stable_release(self):
        self.assertEqual(select_release_tag(["v0.3.13"], ["v0.4.0-rc.1"]), "v0.3.14")
