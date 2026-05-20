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
