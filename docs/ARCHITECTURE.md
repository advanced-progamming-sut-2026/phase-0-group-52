# Code Architecture — Presentation Guide

A walkthrough of how the project is structured and how each system works. Use this to explain the code tomorrow.

---

## 1. Overall architecture: MVC + a command loop

The project follows **Model–View–Controller**:

- **`model.*`** — game state & rules (plants, zombies, quests, game loop, users). No I/O.
- **`view.*`** — printing to the console and reading commands (each menu is a "view").
- **`controller.*`** — glue: parse a command, call the model, ask the view to print.

**The engine is one loop** (`view/AppView.java`):
```java
while (true)
    App.getInstance().getCurrentMenu().checkCommand(scanner);
```
`App` (a **Singleton**, `App.getInstance()`) holds the whole session: current user, current menu, the active `Game`, greenhouse, shop, plant selection. Each menu implements the **`AppMenu`** interface (`check(scanner)`), so the loop is polymorphic — it doesn't care which menu is active. Navigation between menus is centralized in **`controller/Navigation.java`** (it also enforces the login guards).

**Design patterns to name in the demo:** Singleton (`App`), MVC, Factory (`PlantFactory`, `ZombieFactory`), Repository (`UserRepository`, `QuestRepository`), Strategy/polymorphism (plant & zombie behaviors).

---

## 2. Plants — inheritance & polymorphism

`Plants` is an **enum** holding the static data of all **68 plants** (name, category, cost, HP, damage, action-interval, recharge, tags). Behavior lives in a class hierarchy:

```
Plant (abstract)                         ← hp, damage, actionTimer, actionInterval, onTick/onPlanted/onPlantFood
 ├─ Shooter        (15 dedicated subtypes: SnowPea, Repeater, Threepeater, Citron, Starfruit, …)
 ├─ Explosive      (12: CherryBomb, Jalapeno, Squash, PotatoMine, GraveBuster, IcebergLettuce, …)
 ├─ Wallnut        (8: Wall-nut, Tall-nut, Garlic, SweetPotato, SunBean, …)
 ├─ Modifier       (4: Torchwood, Hypno-shroom, Imitater, Lily Pad)
 ├─ StrikeThrough  (2: Cactus, Fume-shroom)
 ├─ SunProducer / Lobber / Melee / Homing   (behavior in the base + a per-type switch)
 └─ Mint           (the 9 "-mint" plants that plant-food their family)
```

**Key design point (say this):** we used **two strategies** deliberately:
1. **Dedicated subclass** when a plant is unique (e.g. `SnowPea` overrides `shoot()` to also slow; `Citron` charges). Clean polymorphism.
2. **Base class + `getType()` switch** for whole families that share one algorithm with small differences (e.g. `Lobber` lobs at the frontmost zombie and just checks the `ICE` tag for Winter Melon; `Melee` handles Chomper vs Bonk Choy inside one class). Avoids 30 near-identical classes.

Every plant plugs into the loop through the **same three hooks**: `onTick(game)` (per-tick behavior), `onPlanted(game)` (e.g. Grave Buster removes a tombstone), `onPlantFood(game)` (the plant-food super-ability). Planting uses the **`PlantFactory`** (Factory pattern) to build the right subclass from the enum.

**Tags** (`PlantTag`: PEA, ICE, FIRE, AOE, WATER, SHROOM, STACK, …) let behavior be data-driven — e.g. `Torchwood` upgrades any `PEA`-tagged shot to fire; water tiles only accept `WATER`-tagged plants.

**Upgrades** (`PlantData` + `plants.json`): each plant has level 2/3/4 buffs (`BUFF_DAMAGE`, `BUFF_HP`, `BUFF_ACTION_INTERVAL`, `BUFF_RECHARGE`, `BUFF_COST`) applied by `applyUpgrades()` when planted.

---

## 3. Zombies — inheritance & the Factory

`Zombies` is an **enum** with the data of all **32 zombies** (name, HP, eat-DPS, speed, wave-cost, armor). Behavior hierarchy:

```
Zombie (abstract)                ← hp, speed, armor, move(), takeDamage(), freeze state
 ├─ BasicZombie                  ← plain walk + eat (used for default/armored/tanky variants)
 └─ WalkingZombie (abstract)     ← shared eat/crush/hypnotize helpers
      ├─ Gargantuar   (crushes plants, throws an Imp at ½ HP)
      ├─ RaZombie     (steals sun from the reserve, drops it on death)
      ├─ WizardZombie (turns a plant into a sheep)
      ├─ ExplorerZombie, OctopusZombie, HunterZombie, Troglobite, DodoRider,
      │  NewspaperZombie, Prospector, Pianist, Juggler, ParasolZombie, …
      └─ PeashooterZombie / WallnutZombie / JalapenoZombie / SquashZombie  (Zombotany heads)
```

**`ZombieFactory`** (Factory pattern) maps each enum value to its class; anything without a special class falls back to `BasicZombie`. Armor is handled in `takeDamage()` (armor HP absorbs first). Freezing is a first-class state: `freezeFor(ticks)` stores the original speed, sets speed 0, and the game loop calls `advanceFreeze()` each tick and **skips a frozen zombie's turn** until it thaws (10 s).

---

## 4. The game engine — a tick-based loop

- **`GameLoop.step(game)`** runs one **tick** (`Game.TICKS_PER_SECOND = 10`, so 1 tick = 0.1 s). Each step: drop/age sun → run every plant's `onTick` → advance/act every zombie (unless frozen) → chapter mechanics → lawnmowers → cooldowns → remove dead zombies → spawn the next wave → check win/lose.
- **`LevelBuilder.build(app, chapter, level)`** constructs the `Game`: builds the field, applies chapter terrain, generates the wave schedule, copies the chosen deck. `buildSpecial(...)` builds a special level.
- **`GameStats`** records kills, plants lost, sun collected, kill-ticks — this feeds **quests** and **Meow Points** at level end.
- The player drives it with `tick [n]` / `advance time -t <n>` — the engine is deterministic and testable.

---

## 5. Chapters & their features

`ChapterType` = `ANCIENT_EGYPT`, `FROSTBITE_CAVES`, `BIG_WAVE_BEACH`, `DARK_AGES`. Each has:
- **Its own zombie pool** (Egypt: Ra, Explorer, Tomb-raiser; Frostbite: Dodo, Hunter, Troglobite; Beach: Fisherman, Octopus, Snorkel; Dark Ages: Wizard, Juggler, King).
- **Terrain** (`LevelBuilder.applyTerrain`): Egypt/Dark → tombstones; Beach → water column (only `WATER` plants, or a Lily Pad first); Frostbite → slippery tile.
- **A mechanic** (`model.mechanics.ChapterMechanics` subclass): e.g. **Dark Ages disables sky-sun** (night); Frostbite melts/chills.
- **Level scaling:** level 1 spawns **only normal zombies** and gives a **5-plant** deck; level 2+ mixes the chapter pool and gives **8** slots, with more/larger waves.

---

## 6. Special (adventure) levels

`Level` is an **abstract strategy** (`extends AttackPattern`) with hooks `checkVictory`, `checkDefeat`, `onTick`, `isPlantAllowed`, `isSkySunEnabled`. Eight concrete rules subclass it:

| Class | Rule |
|---|---|
| `ConveyorBeltLevel` | plants are delivered on a belt (no sun) |
| `SaveOurSeeds` | lose if a protected plant dies |
| `DeadLine` | lose if a zombie crosses a column |
| `TimedWar` | kill N zombies before time runs out |
| `NightOps` | no sky sun |
| `LoveYourPlants` | lose if you lose too many plants |
| `LockedPlantsLevel` | some plants are banned |
| `PlantWhatYouGet` | no sun producers; waves on command |

`GameLoop` just calls `level.checkVictory/checkDefeat(game)` every tick — the **same loop** runs both normal and special levels; only the rule object changes (open/closed principle). Started with `start special -t <type>`.

---

## 7. Quests

- **`QuestDef`** (enum, 19 quests) = the data: name, `QuestCategory` (DAILY/MAIN/EPIC), `QuestPriorities` (CRITICAL→LOW), reward type/amount, target.
- **`QuestProgress` / `QuestState`** = per-user runtime progress.
- **`QuestManager.onLevelEnd(user, quests, game, won)`** evaluates every quest against `GameStats` + board state, and on completion calls **`RewardService`** to grant the reward (auto-claim) and bump the user's quest counters.
- **`QuestService`** ties it together: load state → evaluate → save via **`QuestRepository`** (JSON).
- Shown in the Travel Log, **sorted by priority**. This is a clean example of **data (enum) + strategy (evaluate) + service (orchestration) + repository (persistence)**.

---

## 8. Saving progress & persistence (Repository pattern)

- **`UserRepository`** reads/writes `users.json` through a small hand-written JSON parser (`database/Json.java`). Every user field is persisted: coins, gems, seed packets, `lastChapter`/`lastLevel` (progress), `maxPoint`, `mostMeowPoint`, plant levels, quest counters, difficulty, `stayLoggedIn`.
- **Progress** = `lastChapter`/`lastLevel` on `User`; winning advances them, and they're saved immediately, so the leaderboard and next session reflect it.
- **Stay-logged-in:** on login we flag the user; at startup `App` reads the repository and, if the flag is set, restores the session straight to the main menu — no re-login.
- **One source of truth:** everything went through JSON repositories (we removed an older SQLite path) so the data model is consistent.

---

## 9. Minigames

Five self-contained turn/tick games under `minigame.*`, launched from the Travel Log (`play <name> <level>`), each with 3 levels and win/lose:
- **Beghouled** (match-3; zombies creep in every 3 swaps),
- **Vasebreaker** (break vases → seed packets fall on the ground and vanish; 3 vase types),
- **Wallnut Bowling** (roll bouncing nuts),
- **I, Zombie** (you place the zombies),
- **Zombotany** (defend against plant-headed zombies).

They're intentionally **decoupled** from the main engine (their own tiny state), which keeps the core `Game`/`GameLoop` simple — a good separation-of-concerns talking point.

---

## 10. Scoring — Meow Points

At level end `GameMenuController.awardMeowPoints` reads `GameStats` and awards bonuses via `MeowPointTracker` (perfect defense 500, fast kills, simultaneous kills, fast wave) → updates `mostMeowPoint` and the high score, feeding the leaderboard.

---

### One-line summary to open the presentation
> "It's an MVC console game driven by a single command loop over a Singleton `App`. Game entities (plants, zombies) are enums for data plus a class hierarchy for behavior, built by factories; levels, quests and persistence are each isolated behind their own abstraction (strategy / service / repository), so the tick-based `GameLoop` stays small and every feature slots in without touching it."
