"""Trade Diary - first-release Modrinth setup.

One-shot: patches project metadata (body, license, source_url, etc.),
uploads the icon, then uploads the 4 v0.1.0 jars.

Reads PAT from claude-memory/kuronami-mods/tools/.tokens/.modrinth_token.
"""

from __future__ import annotations
import json
from pathlib import Path
import requests

API = "https://api.modrinth.com/v2"
SLUG = "trade-diary"
VERSION = "0.1.0"

REPO = Path(__file__).resolve().parent.parent  # mod-008-trade-diary
TOKEN_PATH = Path(
    r"C:\Users\naoki\claude-memory\kuronami-mods\tools\.tokens\.modrinth_token"
)
ICON_PATH = REPO / "logo.png"

# (jar_filename_in_release_folder, mc_version, loader)
JARS = [
    ("tradediary-neoforge-1.21.1-0.1.0.jar", "1.21.1", "neoforge"),
    ("tradediary-forge-1.20.1-0.1.0.jar", "1.20.1", "forge"),
    ("tradediary-fabric-1.21.1-0.1.0.jar", "1.21.1", "fabric"),
    ("tradediary-fabric-1.20.1-0.1.0.jar", "1.20.1", "fabric"),
]

BODY = """\
# Trade Diary

Auto-record every villager trade. Your trader's diary, persisted per-world.

## What it does

Every time a trade with a villager (or wandering trader) completes, Trade
Diary appends an entry that captures **what you gave**, **what you got**,
**who you traded with**, **where**, and **when in the world's timeline**.

You open the diary by right-clicking the **Diary** item — craft it from
`Book + Emerald`.

## What's recorded per entry

| Field | Example |
|---|---|
| Result item × count | Emerald × 3 |
| Cost item(s) × count | Wheat × 60 |
| Villager profession + level | Farmer Lv 1 |
| In-game day | Day 0 |
| Coordinates | (-100, 65, 3) |
| Real-time timestamp | (stored, used for merge logic) |

## Smart merging

Shift-clicking through the same trade six times in a row should be one diary
entry, not six near-duplicates. Trade Diary collapses repeats of the same
trade (same villager, same items, same level) within a 5-second window into
a single line that shows the combined counts.

## Per-world data

Entries are saved under `<world>/data/tradediary/<player-uuid>.json` —
each world keeps its own diary, and the in-memory cache is cleared on world
exit so multi-world saves stay clean.

## Filters

Cycle the bottom-right button to filter:
- **All** trades
- **Books** — enchanted books only (handy for librarian hunts)
- **Wandering** — wandering trader trades only

## Loaders

- NeoForge 1.21.1
- Forge 1.20.1
- Fabric 1.21.1
- Fabric 1.20.1

## Dependencies

- **[Patchouli](https://modrinth.com/mod/patchouli)** — required, used for
  the book texture the diary renders on. Trade Diary doesn't bundle the
  texture (Patchouli's license is CC BY-NC-SA 3.0); add Patchouli alongside.

## Source

[GitHub](https://github.com/KURONAMI333/trade-diary)
"""

PROJECT_PATCH = {
    "body": BODY,
    "license_id": "MIT",
    "source_url": "https://github.com/KURONAMI333/trade-diary",
    "issues_url": "https://github.com/KURONAMI333/trade-diary/issues",
    "categories": ["utility", "social"],
    "client_side": "required",
    "server_side": "required",
    "summary": "Auto-record every villager trade with timestamp, location, and trade details. Your own trader's diary.",
}


def headers(token: str) -> dict:
    return {"Authorization": token, "User-Agent": "kuronami-trade-diary-setup/1.0"}


def main() -> None:
    token = TOKEN_PATH.read_text(encoding="utf-8").strip()

    # 1. PATCH project metadata
    print(f"[patch] {API}/project/{SLUG}")
    r = requests.patch(
        f"{API}/project/{SLUG}", headers=headers(token), json=PROJECT_PATCH, timeout=30
    )
    print(f"  status: {r.status_code}")
    if r.status_code not in (200, 204):
        print(f"  body:   {r.text[:400]}")
        raise SystemExit("project PATCH failed")

    # 2. Upload icon
    print(f"[icon] {ICON_PATH.name}")
    with ICON_PATH.open("rb") as fh:
        r = requests.patch(
            f"{API}/project/{SLUG}/icon?ext=png",
            headers={**headers(token), "Content-Type": "image/png"},
            data=fh.read(),
            timeout=30,
        )
    print(f"  status: {r.status_code}")
    if r.status_code not in (200, 204):
        print(f"  body:   {r.text[:400]}")
        raise SystemExit("icon upload failed")

    # 3. Resolve project id
    r = requests.get(f"{API}/project/{SLUG}", headers=headers(token), timeout=20)
    r.raise_for_status()
    project_id = r.json()["id"]
    print(f"[project] id={project_id}")

    # 4. Upload 4 jars
    release_dir = REPO / "_release" / f"v{VERSION}"
    for jar_name, mc, loader in JARS:
        jar = release_dir / jar_name
        vstr = f"{VERSION}+{loader}-{mc}"
        meta = {
            "name": vstr,
            "version_number": vstr,
            "changelog": "Initial release. See https://github.com/KURONAMI333/trade-diary/releases/tag/v0.1.0",
            "dependencies": [],
            "game_versions": [mc],
            "version_type": "release",
            "loaders": [loader],
            "featured": False,
            "project_id": project_id,
            "file_parts": ["file"],
            "primary_file": "file",
            "status": "listed",
        }
        print(f"[upload] {vstr}")
        with jar.open("rb") as fh:
            files = {
                "data": (None, json.dumps(meta), "application/json"),
                "file": (jar.name, fh, "application/java-archive"),
            }
            r = requests.post(
                f"{API}/version", headers=headers(token), files=files, timeout=120
            )
        print(f"  status: {r.status_code}")
        if r.status_code not in (200, 201):
            print(f"  body:   {r.text[:400]}")
            raise SystemExit(f"version upload failed for {jar_name}")


if __name__ == "__main__":
    main()
