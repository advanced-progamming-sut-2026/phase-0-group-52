# Test Cases — All Quests (19)

Quests are tracked in the **Travel Log** and evaluated automatically at the end of every level (`QuestService.onLevelEnd`). Rewards are granted the moment a quest completes (auto-claimed).

**View progress** (grouped/sorted by priority):
```
menu enter travel_log_menu
travel log page daily      # DAILY quests
travel log page main       # MAIN quests
travel log page epic       # EPIC quests
```
Each line: `[PRIORITY] Name   progress: x/y | reward: N TYPE | in progress|completed|claimed`.

**General verify loop:** play a level, satisfy the condition, win/lose as required, then re-open the matching `travel log page …` to see progress advance / status become `claimed`.

Helpers used below: `cheat sun <n>`, `cheat remove-cooldown`, `cheat spawn-zombie -t <t> -l (x,y)`, `cheat add-plant-food`, `pluck plant -l (x,y)`.

---

## DAILY quests (`travel log page daily`)
| Quest | Reward | Condition | How to trigger |
|---|---|---|---|
| **Daily Sun Collector** | 30 coin | collect **3000** sun total (accumulates) | plant Sunflowers, `collect sun` repeatedly over levels |
| **Professional Plant User** | 1 plant-unlock | kill **10** zombies using only **one** plant type | deck of a single shooter; kill 10 |
| **Only Cactus** | 20 gem | kill **10** zombies using only **Cactus** | plant only Cactus; kill 10 |
| **Demolition Pro** | 100 coin | use **3** explosive plants | plant Cherry Bomb + Jalapeno + Squash in a level |
| **Symmetry** | 500 coin | **win** with a vertically symmetric board | mirror your plants top/bottom, then win |
| **Family Kill** | 1000 coin | **win** using only one plant family (+ ≥1 kill) | deck all from one category; win |
| **Bloom in Limits** | 100 gem | **win** without using a specific family | avoid that family; win |
| **Win Streak** | 5000 coin | **win 5** levels in a row at **max difficulty (5)** | `menu settings change-difficulty -l 5`; win ×5 |
| **Almost Won** | 300 coin | **10** kills at column 0 with **no lawnmower** | let zombies reach col 1 and kill them there |
| **No OCD** | 800 coin | **win** with an **asymmetric** board | uneven plant layout, then win |
| **Cloudy Day** | 10 gem | **win** using exactly **3 sun-producers** (all sun) | deck of 3 sunflowers only; win |
| **One Column Less** | 10 gem | **win** with a whole **column empty** | leave one column unplanted; win |
| **Defenseless Row** | 20 gem | **win** with a whole **row empty** | leave one row unplanted; win |
| **Defenseless Cross** | 25 gem | **win** with a **column AND a row** empty | leave one col + one row empty; win |

## MAIN quests (`travel log page main`)
| Quest | Reward | Condition | How to trigger |
|---|---|---|---|
| **Chapter Hunter** | 10 seed-packet | kill **50** zombies in a given chapter | play that chapter, rack up 50 kills |
| **Thrifty Herbivore** | 20 seed-packet | **win** losing **≤ N** plants | win a level with few/no plants eaten |
| **Quick Kills** | 500 coin | **10** kills within **30 s** of the first wave | burst-kill early with Cherry/Jalapeno |

## EPIC quests (`travel log page epic`)
| Quest | Reward | Condition | How to trigger |
|---|---|---|---|
| **Defense Master** | 200 gem | **win** with **final sun = 0** | spend all your sun before winning |
| **Night or Day** | 20 gem | **win** a day level using only **night/shroom** plants | deck of shrooms; win in Egypt |

---

## Fast end-to-end demo (shows the connection + auto-reward)
```
menu enter chapter_menu
menu enter chapter -c ANCIENT_EGYPT -l 1
menu enter choose_plant_menu
add plant -t Peashooter
add plant -t Sunflower
start
cheat sun 1000
cheat remove-cooldown
... plant, leave a whole column empty, collect sun, tick until "You win!" ...
menu enter travel_log_menu
travel log page daily
```
Expected: `Daily Sun Collector` progress advanced (e.g. `250/3000`), and layout-based quests like **One Column Less** / **No OCD** show **claimed** with the reward granted.

**Rubric:** نمایش کوئست‌ها ✅ · نمایش پیشرفت ✅ · عملکرد کوئست‌ها (auto-evaluate on level end) ✅ · دریافت جایزه (auto-grant + claimed) ✅ · اولویت کوئست (sorted CRITICAL→HIGH→MEDIUM→LOW) ✅.
