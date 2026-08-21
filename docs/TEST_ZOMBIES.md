# Test Cases — All Zombies (32)

**Common setup** (start any level, then spawn zombies with the cheat):
```
menu enter chapter_menu
menu enter chapter -c ANCIENT_EGYPT -l 1
menu enter choose_plant_menu
add plant -t Peashooter
start
cheat sun 2000
cheat remove-cooldown
```
Spawn: `cheat spawn-zombie -t <alias> -l (<x>, <y>)`  ·  observe with `tick <n>` and `zombies info`.
`x` is the column (9 = far right), `y` the row (1–5).

Legend: **Basic** = walks toward the house and eats plants (armored variants absorb damage in the armor first).

| # | alias | HP | Behavior to demonstrate | How to see it |
|---|---|---|---|---|
| 1 | `default` | 190 | Basic walk + eat | plant a Peashooter same row → it dies; or `tick` until it eats a plant |
| 2 | `armor1` | 190 (+cone) | Conehead — armor absorbs first | `zombies info` shows armor; takes longer to kill |
| 3 | `armor2` | 190 (+bucket) | Buckethead — tougher armor | same, more shots to kill |
| 4 | `armor4` | 190 (+brick) | Brickhead — toughest armor | same |
| 5 | `gargantuar` | 3600 | **Smashes** plants instantly + **throws an Imp** at ½ HP | damage it below 1800 → `The Gargantuar threw an Imp into row X!` |
| 6 | `imp` | 190 | Small, fast (speed 0.22) | spawns/moves quickly |
| 7 | `ra` | 190 | **Steals sun from your reserve**, drops it back on death | `cheat sun 2000` then `tick 30` → `Zombie Ra stole 25 sun!`; kill it → `dropped … sun back` |
| 8 | `explorer` | 250 | **Torch destroys the plant in front** | plant in its row ahead → `... is destroyed` as it passes |
| 9 | `tomb_raiser` | 380 | Basic (tanky) | walk + eat |
| 10 | `ice_age_dodo` | 490 | **Flies over** front plants/obstacles | plant a Wall-nut in its row → it jumps over |
| 11 | `ice_age_hunter` | 700 | **Throws ice** that freezes a plant from range | plant ahead → plant gets frozen before contact |
| 12 | `ice_age_troglobite` | 470 | **Pushes an ice block** that crushes plants | plant ahead → crushed by the block |
| 13 | `barrel_roller` | 470 (+barrel 1100) | Rolls a barrel; tanky | tick, tanky |
| 14 | `beach_fisherman` | 1000 | Basic (very tanky) | walk + eat |
| 15 | `beach_octopus` | 910 | **Throws an octopus that disables a plant** | plant ahead → it stops working |
| 16 | `beach_snorkel` | 350 | Basic | walk + eat |
| 17 | `dark_armor3` | 190 (+knight) | Knight armor (shoulder+crown) | tanky armor |
| 18 | `dark_juggler` | 420 | **Throws projectiles at your plants** | plant ahead → `Wall-nut` HP drops from range (~40/2s) |
| 19 | `wizard` | 490 | **Casts a spell turning a plant into a sheep** | plant several → one becomes harmless |
| 20 | `dark_king` | 1000 | Basic (buff king, tanky) | walk + eat |
| 21 | `dark_imp_dragon` | 190 | Fast imp variant | quick |
| 22 | `modern_all_star` | 1100 | Basic (tanky) | walk + eat |
| 23 | `lost_city_jane` | 350 | **Parasol deflects lobbed shots** | Melon/Cabbage-pult in its row → 0 damage (vs a normal zombie which takes damage) |
| 24 | `crystal_skull` | 250 | Turquoise — laser/steal | tick to see effect |
| 25 | `prospector` | 190 | **Rocket-jumps to the back** after a fuse | `tick` ~ 10s → teleports to column 0 |
| 26 | `piano` | 840 | Pianist — very high eat DPS (4000) | eats plants fast |
| 27 | `newspaper` | 460 (+paper 800) | **Enrages (2×–3× speed/damage) when the paper is destroyed** | strip its armor → `enraged`, speeds up |
| 28 | `arcade` | 490 | Arcade machine (1100) — tanky | tick |
| 29 | `peashooter_head` | 190 | **Zombotany: shoots peas** at your plants | plant ahead → takes shot damage |
| 30 | `wallnut_head` | 4000 | **Zombotany: extremely tanky** | many shots to kill |
| 31 | `jalapeno_head` | 190 | **Zombotany: fuse then explodes** | `tick` ~10s → row blast |
| 32 | `squash_head` | 190 | **Zombotany: crushes a plant** on contact | plant ahead → instantly destroyed |

**Rubric:** نمایش اطلاعات زامبی → `zombies info` / `menu collection show-zombie -z <name>` · تقلب افزودن زامبی → `cheat spawn-zombie` · زامبی‌های اجباری → all above implemented.
