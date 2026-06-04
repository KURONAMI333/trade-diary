# Trade Diary

A trader's diary that records every villager trade — what you gave, what you got, who you traded with, where, and when — persisted per world.

Open it by right-clicking the **Diary** item, crafted from `book + emerald`. Each completed trade with a villager or wandering trader appends an entry:

- Result item × count, and cost item(s) × count
- Villager profession and level (e.g. Farmer Lv 1)
- In-world day and coordinates

Shift-clicking the same trade six times in a row should be one diary entry, not six. Trade Diary collapses repeats of the same trade (same villager, items, and level) within a 5-second window into one line with the combined counts. The bottom-right button filters entries: all, enchanted books only (handy for librarian hunts), or wandering-trader only.

Entries are saved per world under `<world>/data/tradediary/<player-uuid>.json`, and the in-memory cache is cleared on world exit so multi-world saves stay separate. The viewer is a spread-page book styled after Patchouli.

**Dependencies**

- [Patchouli](https://modrinth.com/mod/patchouli) — required; it provides the book texture the diary renders on. Trade Diary doesn't bundle it (Patchouli is CC BY-NC-SA), so add it alongside.

Install on the server and on each client — the server records trades, the diary viewer is client-side.

Free to use in any modpack. Source and issues: https://github.com/KURONAMI333/trade-diary
