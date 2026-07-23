# Game Constants Reference

All `static final` constants in the codebase, grouped by area. Values are taken directly from source.

---

## Time / Tick model

The engine is tick-based. One `GameLoop.step()` call = one tick.

| Constant | Value | File | Meaning |
|---|---|---|---|
| `Game.TICKS_PER_SECOND` | `10` | `model/Game.java` | 1 tick = **0.1 s** (100 ms) |
| `Game.SECONDS_PER_TICK` | `1.0 / 10` = `0.1` | `model/Game.java` | seconds advanced per tick |

Helpers in `Game`:
- `secondsToTicks(sec) = round(sec * 10)`
- `ticksToSeconds(ticks) = ticks / 10`

**All entity timers are now second-based:** every `Timer += Game.SECONDS_PER_TICK` per tick, and zombie movement is `position.x -= speed * Game.SECONDS_PER_TICK`. So plant/zombie constants below (`actionInterval`, `SHOT_INTERVAL`, `LIFESPAN`, `ARM_TIME`, recharge, zombie `speed`, eat DPS) are all in **real seconds / cells-per-second**.

### Timers that DO honor the 10:1 conversion

| Constant | Value | Real time | File |
|---|---|---|---|
| `Sun.FALL_TICKS` | `5 * TICKS_PER_SECOND` = 50 ticks | 5 s | `model/entities/Sun.java` |
| `GameLoop.WAVE_INTERVAL` | `15 * TICKS_PER_SECOND` = 150 ticks | 15 s | `model/GameLoop.java` |
| Sky sun interval (doc formula) | `x = max(6 + 0.05t, 12)` s, `t` = seconds since start | 12 s early, grows after t=120 s | `GameLoop.skyIntervalTicks` |
| `QuestManager.THIRTY_SECONDS_TICKS` | `300` ticks | 30 s | `model/quest/QuestManager.java` |
| `ConveyorBeltLevel.DELIVERY_INTERVAL` | `12` ticks | 1.2 s | `model/level/ConveyorBeltLevel.java` |

Waves spawn at 15 s, 30 s, 45 s, … (`tick >= (waveIndex+1) * WAVE_INTERVAL`). Sky sun drops on the doc's schedule `max(6 + 0.05t, 12)` s (every 12 s early game), falls for 5 s, then can be collected. Falling sun types: NORMAL 25 (80%), SPECIAL 100 (15%), RADIOACTIVE 150 (5%; explodes if collected mid-fall — 150 to zombies in 5×5, 80 to plants in 3×3 — else becomes a normal sun on landing).

---

## Board / field dimensions

| Constant | Value | File |
|---|---|---|
| `GameField.ROWS` | `5` | `model/GameField.java` |
| `GameField.COLS` | `9` | `model/GameField.java` |
| `Greenhouse.ROWS` | `4` | `model/greenhouse/Greenhouse.java` |
| `Greenhouse.COLS` | `5` | `model/greenhouse/Greenhouse.java` |
| Minigame boards (`Beghouled`, `Vasebreaker`, `WallnutBowling`, `IZombie`) | `ROWS 5`, `COLS 9` | `minigame/*.java` |
| `WallnutBowling.PLANT_ZONE` | `3` | `minigame/WallnutBowling.java` |
| `IZombie.RED_LINE` | `3` | `minigame/IZombie.java` |

---

## Core gameplay limits

| Constant | Value | File |
|---|---|---|
| `Game.MAX_PLANT_FOOD` | `3` | `model/Game.java` |
| `PeaPod.MAX_HEADS` | `5` | `model/entities/plants/types/PeaPod.java` |
| `ChoosePlantMenuController.MAX_SLOTS` | `7` | `controller/menu/ChoosePlantMenuController.java` |

---

## Plant progression / collection

| Constant | Value | File |
|---|---|---|
| `PlantData.MAX_LEVEL` | `4` | `model/entities/plants/PlantData.java` |
| `PlantData.SEED_PACKETS_PER_LEVEL` | `5` | `model/entities/plants/PlantData.java` |
| `PlantData.COINS_PER_LEVEL` | `500` | `model/entities/plants/PlantData.java` |
| `CollectionMenuController.BUY_COST_PER_SUN` | `10` | `controller/menu/CollectionMenuController.java` |

---

## Shop / economy

| Constant | Value | File |
|---|---|---|
| `Shop.POT_PRICE` | `2000` | `model/shop/Shop.java` |
| `Shop.PLANT_FOOD_PRICE` | `3` | `model/shop/Shop.java` |
| `Shop.PLANT_FOOD_MAX` | `3` | `model/shop/Shop.java` |
| `Shop.RANDOM_SEEDS_PRICE` | `1000` | `model/shop/Shop.java` |
| `Shop.RANDOM_SEEDS_COUNT` | `5` | `model/shop/Shop.java` |
| `Shop.CHOICE_SEEDS_PRICE` | `5` | `model/shop/Shop.java` |
| `Shop.CHOICE_SEEDS_COUNT` | `10` | `model/shop/Shop.java` |
| `Shop.EXCHANGE_DIAMONDS` | `5` | `model/shop/Shop.java` |
| `Shop.EXCHANGE_COINS` | `500` | `model/shop/Shop.java` |
| `Shop.DAILY_BASE_PRICE` | `2000` | `model/shop/Shop.java` |
| `Shop.DAILY_PRICE` | `1600` | `model/shop/Shop.java` |
| `Shop.DAILY_COUNT` | `10` | `model/shop/Shop.java` |

---

## Greenhouse

| Constant | Value | File |
|---|---|---|
| `Greenhouse.MARIGOLD_REWARD` | `500` coins | `model/greenhouse/Greenhouse.java` |
| `Greenhouse.HOUR_MILLIS` | `3600 * 1000` (1 h) | `model/greenhouse/Greenhouse.java` |

---

## Scoring / Meow points (mechanics)

| Constant | Value | File |
|---|---|---|
| `MeowPointTracker.MULTI_KILL_BONUS` | `100` | `model/mechanics/MeowPointTracker.java` |
| `MeowPointTracker.FAST_KILL_BONUS` | `50` | `model/mechanics/MeowPointTracker.java` |
| `MeowPointTracker.SIMULTANEOUS_KILL_BONUS` | `75` | `model/mechanics/MeowPointTracker.java` |
| `MeowPointTracker.FAST_WAVE_BONUS` | `200` | `model/mechanics/MeowPointTracker.java` |
| `MeowPointTracker.PERFECT_DEFENSE_BONUS` | `500` | `model/mechanics/MeowPointTracker.java` |
| `DarkAgesMechanics.GRAVE_SUN_BONUS` | `50` | `model/mechanics/DarkAgesMechanics.java` |
| `FrostbiteCavesMechanics.MELT_RATE` | `60` | `model/mechanics/FrostbiteCavesMechanics.java` |

---

## Quests

| Constant | Value | File |
|---|---|---|
| `QuestManager.MAX_DIFFICULTY` | `5` | `model/quest/QuestManager.java` |
| `QuestManager.THIRTY_SECONDS_TICKS` | `300` ticks (30 s) | `model/quest/QuestManager.java` |

---

## Minigames

| Constant | Value | File |
|---|---|---|
| `Beghouled.SUN_PER_MATCH_UNIT` | `50` | `minigame/Beghouled.java` |
| `Beghouled.UPGRADE_COSTS` | `{500, 1500, 500, 250, 1000, 750}` | `minigame/Beghouled.java` |
| `WallnutBowling.ZOMBIE_HP` | `190` | `minigame/WallnutBowling.java` |
| `IZombie.COST` | `{50, 75, 125, 25, 150}` | `minigame/IZombie.java` |

---

## Selected plant timing / damage

`actionInterval` (last-but-one number in the `Plants` enum) and the per-type constants below are the plant "clock" values. They are consumed as **ticks** (`+1` per tick). Recharge is the final enum number.

| Plant | actionInterval | Notes |
|---|---|---|
| Sunflower / Twin / Sun-shroom / Primal | `7` | sun production cadence (every 7 s) |
| Peashooter & most shooters | `1.5` | shot cadence |
| Citron | `9` | charged shot |
| Bowling Bulb | `2` | `DAMAGE {40,120,180}`, `DELAY {2,5,10}` |
| Caulipower / Electric Blueberry | `12` | homing |

Per-type plant constants:

| Constant | Value | File |
|---|---|---|
| `Mint.LIFESPAN` | `5` | `types/Mint.java` |
| `PuffShroom.LIFESPAN` / `RANGE` | `60` / `3` | `types/PuffShroom.java` |
| `SeaShroom.LIFESPAN` / `RANGE` | `60` / `3` | `types/SeaShroom.java` |
| `FumeShroom.RANGE` | `4` | `types/FumeShroom.java` |
| `PrimalPotatoMine.ARM_TIME` | `5` | `types/PrimalPotatoMine.java` |
| `Grapeshot.BOUNCES` / `BOUNCE_DAMAGE` | `6` / `300` | `types/Grapeshot.java` |
| `SunBean.SUN_PER_HIT` | `5` | `types/SunBean.java` |
| `SweetPotato.PULL_RANGE` | `2` | `types/SweetPotato.java` |

---

## Selected zombie stats

Format in `Zombies` enum: `(name, hp, eatDPS, speed, waveCost, weight, armor)`. `speed` is **cells subtracted from x per tick**.

| Zombie | HP | speed | waveCost |
|---|---|---|---|
| `ZOMBIE_DEFAULT` | 190 | 0.185 | 100 |
| `ZOMBIE_ARMOR1` (conehead) | 190 | 0.185 | 200 |
| `ZOMBIE_ARMOR2` (buckethead) | 190 | 0.185 | 400 |
| `ZOMBIE_GARGANTUAR` | 3600 | 0.24 | 1500 |
| `ZOMBIE_IMP` | 190 | 0.22 | 100 |
| `ZOMBIE_WALLNUT_HEAD` | 4000 | 0.185 | 300 |
| `ZOMBIE_SQUASH_HEAD` | 190 | 0.4 | 300 |

Per-type zombie constants:

| Constant | Value | File |
|---|---|---|
| `PeashooterZombie.SHOT_INTERVAL` / `SHOT_DAMAGE` | `1.5` / `20` | `types/PeashooterZombie.java` |
| `HunterZombie.THROW_INTERVAL` | `3` | `types/HunterZombie.java` |
| `OctopusZombie.THROW_INTERVAL` | `5` | `types/OctopusZombie.java` |
| `WizardZombie.CAST_INTERVAL` | `5` | `types/WizardZombie.java` |
| `PianistZombie.SHUFFLE_INTERVAL` | `4` | `types/PianistZombie.java` |
| `JalapenoZombie.FUSE_TIME` | `10` | `types/JalapenoZombie.java` |
| `ProspectorZombie.FUSE_TIME` | `10` | `types/ProspectorZombie.java` |
| `NewspaperZombie.NEWSPAPER_HP` / `RAGE_FACTOR` | `190` / `3` | `types/NewspaperZombie.java` |
| `BarrelRoller.BARREL_HP` | `1100` | `types/BarrelRoller.java` |
| `ArcadeZombie.MACHINE_HP` | `1100` | `types/ArcadeZombie.java` |
| `Troglobite.ICE_HP` | `470` | `types/Troglobite.java` |
| `RaZombie.SUN_PER_ORB` / `MAX_STOLEN` | `25` / `250` | `types/RaZombie.java` |
| `TurquoiseZombie.STEAL_PER_SECOND` / `STEAL_DURATION` / `LASER_RANGE` | `25` / `5` / `4` | `types/TurquoiseZombie.java` |

---

## Note: per-entity timing units (now unified)

All time-based counters advance in **real seconds**:

- Entity timers: `Timer += Game.SECONDS_PER_TICK` each tick, compared against second-valued constants.
- Zombie movement: `position.x -= speed * Game.SECONDS_PER_TICK` (so `speed` = cells/second).
- Eating DPS: `takeDamage(getDamage() * Game.SECONDS_PER_TICK)` per tick.
- Plant recharge: cooldown counts down by `SECONDS_PER_TICK` each tick, so `recharge` values are seconds and the "ready in X.Xs" message is accurate.

```java
// PeashooterZombie.onTick — now fires every 1.5 real seconds
shotTimer += Game.SECONDS_PER_TICK;
while (shotTimer >= SHOT_INTERVAL) { ... }
```

Loop-level scheduling still uses the raw tick counter, with intervals expressed as `<seconds> * Game.TICKS_PER_SECOND` (see `Sun.FALL_TICKS`, `WAVE_INTERVAL`, `SKY_SUN_INTERVAL`).
