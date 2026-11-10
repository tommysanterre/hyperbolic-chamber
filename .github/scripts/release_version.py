"""Choose a stable release tag, reusing the commit's tag on workflow retries."""

import re
import subprocess


def select_release_tag(tags: list[str], commit_tags: list[str]) -> str:
    def version(tag: str) -> tuple[int, ...] | None:
        if re.fullmatch(r"v(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)", tag):
            return tuple(map(int, tag[1:].split(".")))
        return None

    existing = [tag for tag in commit_tags if version(tag) is not None]
    if existing:
        return max(existing, key=version)

    latest = max((version(tag) for tag in tags if version(tag) is not None), default=(0, 0, 0))
    major, minor, patch = latest
    return f"v{major}.{minor}.{patch + 1}"


if __name__ == "__main__":
    def git_tags(*args: str) -> list[str]:
        return subprocess.check_output(["git", "tag", *args], text=True).splitlines()

    print(select_release_tag(git_tags(), git_tags("--points-at", "HEAD")))
