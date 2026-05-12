# Trade Diary (TD)

> Auto-record every villager trade with timestamp, location, and trade details — your own trader's diary.

[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Modrinth](https://img.shields.io/badge/Modrinth-trade--diary-00AF5C)](https://modrinth.com/mod/trade-diary)
[![CurseForge](https://img.shields.io/badge/CurseForge-trade--diary-F16436)](https://www.curseforge.com/minecraft/mc-mods/trade-diary)

---

## What it does

Every time a trade with a villager (or wandering trader) completes, Trade
Diary appends an entry that captures **what you gave**, **what you got**,
**who you traded with**, **where**, and **when in the world's timeline**.

You open the diary by right-clicking the **Diary** item — craft it from
`Book + Emerald` (shapeless).

## What's recorded per entry

| Field | Example |
|---|---|
| Result item × count | `Emerald × 3` |
| Cost item(s) × count | `Wheat × 60` (or `Book × 1 + Emerald × 5` for librarians) |
| Villager profession + level | `Farmer Lv 1` |
| In-game day | `Day 0` |
| Coordinates | `(-100, 65, 3)` |
| Real-time timestamp | stored, used for merge logic |

## Smart merging

Shift-clicking through the same trade six times in a row is **one** diary
entry, not six near-duplicates. Repeats of the same trade (same villager,
same items, same level) within a 5-second window collapse into a single
line with combined counts.

## Per-world data

Entries are saved under `<world>/data/tradediary/<player-uuid>.json` —
each world keeps its own diary, and the in-memory cache is cleared on
world exit so multi-world saves stay clean.

## Filters

Cycle the bottom-right button to filter:

- **All** trades
- **Books** — enchanted books only (handy for librarian hunts)
- **Wandering** — wandering trader trades only

---

## Supported Loaders / Versions

| Minecraft | NeoForge | Forge | Fabric |
|---|:---:|:---:|:---:|
| 1.21.1 | ✅ | — ¹ | ✅ |
| 1.20.1 |  —  | ✅ | ✅ |

¹ *Forge 1.21.1 is skipped because Patchouli (the dependency used for the
book texture) has no Forge 1.21.1 release. NeoForge 1.21.1 covers the
same Minecraft version.*

## Installation

1. Install your loader (NeoForge / Forge / Fabric) for the target MC version.
2. Install **Patchouli** for that loader (required — Trade Diary uses
   Patchouli's `book_brown.png` texture for the spread-page viewer).
3. Drop the matching `tradediary-{loader}-{mc}-{version}.jar` into your
   `mods/` folder.
4. Launch — craft a Diary from `Book + Emerald`, trade with a villager,
   right-click the Diary to open.

## Dependencies

| Mod | Required? | Why |
|---|---|---|
| [Patchouli](https://modrinth.com/mod/patchouli) | **Required** | Provides the `book_brown.png` texture the diary renders on |

Patchouli is **not bundled** — it's CC BY-NC-SA 3.0 licensed, so add it
alongside Trade Diary.

---

## Design notes

- **Trade capture**: Forge uses the first-class `TradeWithVillagerEvent`;
  Fabric (which has no such event) uses a Mixin into
  `AbstractVillager#notifyTrade`. Both fire server-side right after vanilla
  applies the trade.
- **Persistence**: 16-field `TradeEntry` record persisted via DataFixerUpper
  `Codec` to per-player JSON. Field count is capped at 16 because
  `RecordCodecBuilder.group()` maxes out there.
- **No keybinding**: The Diary item itself is the open affordance — one
  less hotkey to manage, no collision with the vanilla advancement screen
  (which already uses `L` in many setups).
- **Cache hygiene**: An in-memory cache (keyed by player UUID) backs the
  JSON store, and it's explicitly cleared on `ServerStoppedEvent` /
  `SERVER_STOPPED` so leaving one single-player world and entering another
  doesn't show stale entries from the previous world.

## Building from source

```bash
# NeoForge 1.21.1 (main)
./gradlew build

# Per-loader sub-projects
cd forge-1.20.1   && ./gradlew build
cd fabric-1.21.1  && ./gradlew build
cd fabric-1.20.1  && ./gradlew build
```

Each sub-project produces a single jar under its own `build/libs/`.

## License

[MIT License](LICENSE) — modpack inclusion welcome, no credit required.

## Credits

- Author: KURONAMI
- Texture dependency: [Vazkii's Patchouli](https://github.com/VazkiiMods/Patchouli)
  (CC BY-NC-SA 3.0)
