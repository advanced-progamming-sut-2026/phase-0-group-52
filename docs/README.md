# Project Docs — Index & Presentation Cheat-Sheet

Plants vs. Zombies 2 — Advanced Programming project. Start here.

## The documents
| Doc | What it's for |
|---|---|
| [ARCHITECTURE.md](ARCHITECTURE.md) | **How the code is built** — MVC, inheritance, design patterns. Read this to explain the code. |
| [COMMANDS.md](COMMANDS.md) | Full command reference. |
| [TEST_CASES.md](TEST_CASES.md) | Linear demo script (13 sections) — run top-to-bottom in the presentation. |
| [TEST_PLANTS.md](TEST_PLANTS.md) | How to test each of the 68 plants. |
| [TEST_ZOMBIES.md](TEST_ZOMBIES.md) | How to test each of the 32 zombies. |
| [TEST_QUESTS.md](TEST_QUESTS.md) | How to trigger each of the 19 quests. |
| [MINIGAMES.md](MINIGAMES.md) | Rules & commands for the 5 minigames. |
| [CONSTANTS.md](CONSTANTS.md) | All tunable constants (timing, sizes, prices). |
| [RUBRIC_STATUS.md](RUBRIC_STATUS.md) | Every rubric item mapped to its status. |

---

## Presentation cheat-sheet

### One-line opener
> "It's an MVC console game driven by a single command loop over a Singleton `App`. Entities (plants, zombies) are **enums for data + a class hierarchy for behavior**, built by **factories**; levels, quests and persistence each sit behind their own abstraction (**Strategy / Service / Repository**), so the tick-based `GameLoop` stays small and every feature slots in without touching it."

### Design patterns to name
- **Singleton** — `App.getInstance()` holds the whole session.
- **Factory** — `PlantFactory`, `ZombieFactory` build the right subclass from an enum.
- **Repository** — `UserRepository`, `QuestRepository` persist to JSON (`database/Json.java`).
- **Strategy / polymorphism** — plant, zombie and `Level` behaviors are interchangeable subclasses.
- **MVC** — `model` (rules) / `view` (console) / `controller` (glue), joined by the `AppMenu` interface + `Navigation`.

### The 5 key talking points
1. **Two strategies for plant behavior** (deliberate design):
   - a *dedicated subclass* for a unique plant (`SnowPea` slows, `Citron` charges);
   - a *base class + `getType()` switch* for a whole family sharing one algorithm (`Lobber`, `Melee`) — avoids ~30 near-identical classes.
2. **enum = data, class = behavior.** `Plants` / `Zombies` / `QuestDef` are pure data; behavior lives in the class hierarchy.
3. **One loop for all levels.** `GameLoop.step` runs normal *and* special levels; only the `Level` object changes (Open/Closed principle).
4. **Everything hooks the same 3 methods** on `Plant`: `onTick`, `onPlanted`, `onPlantFood`. New plant = new subclass, zero engine changes.
5. **Persistence is one source of truth** — all state in JSON via repositories; stay-logged-in restores the session at startup.

### Suggested live-demo order (from TEST_CASES.md)
1. sign-up → security question → login (stay-logged-in) → forgot password
2. menus + navigation guards + profile edit
3. news + difficulty
4. collection (show plants/zombies, upgrade, buy) + wallets + leaderboard
5. choose a 5-plant deck (level 1) → start
6. core gameplay: sun, plant, waves, cheats, **win** (see the zombie taunt + Meow Points + loot drops)
7. show a few special plant/zombie behaviors (Gargantuar imp, Ra sun-steal, Squash crush, Iceberg freeze)
8. quests (travel log, priority, auto-reward)
9. all 5 minigames
10. 4+ special levels (`start special -t …`)
11. greenhouse + shop + boost

### If asked "what did *you* design vs the team?"
Be honest: it's a group project built collaboratively (including AI assistance for parts of the implementation and a lot of bug-fixing and hardening). Focus your answers on *how the code works and why it's structured this way* — that's what the architecture above explains.
