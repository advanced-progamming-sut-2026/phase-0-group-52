# Minigames — Documentation

There are **5 minigames**, each with **3 levels** (1–3) and full win/lose. Launch from the Travel Log:

```
menu enter travel_log_menu
travel log page minigame          # lists the 5 minigames
play <name> <level>               # e.g. play beghouled 1
```
Valid `<name>`: `beghouled`, `vase_breaker`, `wallnut_bowling`, `i_zombie`, `zombotany`.

> In every minigame that has `tick`, you can also write `tick <n>` to advance **n** steps at once (e.g. `tick 40`). Beghouled advances by making swaps (no tick).

---

## 1. Beghouled  (`play beghouled <1-3>`)
A match-3 board of plants. Swap adjacent plants to make lines of 3+; matches earn sun and clear tiles. Zombies creep in; upgrade plants with earned sun.

**Commands**
```
show                          # print the board + sun + match count
swap <x1> <y1> <x2> <y2>       # swap two adjacent plants
upgrades                      # list available plant upgrades & costs
upgrade <plant>               # buy an upgrade with sun
exit                          # leave the minigame
```
**Win:** make **10** matches of 3+.  **Lose:** a zombie reaches the house.
**Reward on win:** coins (scaled by level) + `miniGamesPlayed++`.

---

## 2. Vasebreaker  (`play vase_breaker <1-3>`)
Break vases to reveal plants or zombies. Some vases hide plants you can then place, some hide zombies (including a Gargantuar vase).

**Commands**
```
break (x, y)                  # smash the vase at that tile
plant -t <type> -l (x, y)     # place a plant revealed from a vase
tick                          # advance time (zombies move)
show                          # print the board
exit
```
The board shows each vase's type so specials are identifiable: **`p`** = plant (seed-packet) vase, **`@`** = Gargantuar vase, **`#`** = zombie vase, **`o`** = empty vase.
**Win:** break **all vases** without a zombie reaching the house.  **Lose:** a zombie reaches the house.
Rubric items: اثر شکستن کوزه (vase effect), خواص seed packet, کوزه گیاه (plant vase `p`), کوزه غول (gargantuar vase `@`).

---

## 3. Wallnut Bowling  (`play wallnut_bowling <1-3>`)
Roll wall-nuts down the lanes; they bounce diagonally and crush zombies. Explode-o-nuts blast an area.

**Commands**
```
plant -l (x, y)               # roll the next nut from the conveyor onto row y (cols 1-3)
tick                          # advance; nuts roll & bounce, zombies advance
show                          # print the board
exit
```
**Win:** destroy the target number of zombies (`8 + 4*level`).  **Lose:** a zombie reaches the house.
Rubric: عملکرد مناسب گردوها (nut behavior), چند مرحله, برد/باخت.

---

## 4. I, Zombie  (`play i_zombie <1-3>`)
Reverse mode: **you place the zombies** and must get one past the plant defense to the left edge.

**Commands**
```
place -t <default|armor1|armor2|imp|gargantuar> -l (x, y)   # spend sun to place a zombie
tick                          # advance; zombies walk left, plants shoot
show
exit
```
**Win:** get a zombie to the left edge (eat the last brain).  **Lose:** run out of sun/zombies with defense intact.
Rubric: امکان گذاشتن زامبی (place zombie), چند مرحله, برد/باخت.

---

## 5. Zombotany  (`play zombotany <1-3>`)  — built this phase
Defend against **plant-headed zombies**. Each behaves like its plant head:
- **Peashooter-zombie (S):** shoots your plants from range.
- **Wall-nut-zombie (N):** very tanky (800 HP).
- **Jalapeno-zombie (J):** blasts its whole row when it dies.
- **Squash-zombie (Q):** crushes a plant instantly on contact.

**Commands**
```
plant -t <peashooter|wallnut> -l (x, y)   # plant defenders in columns 1-3
tick
show
exit
```
**Win:** destroy `6 + 4*level` zombies → `You cleared the plant-headed horde. You win! Reward: 300*level coins`.  **Lose:** a zombie reaches the house.
Rubric: زامبی تیرانداز/گردو/فلفل/کدو, چند مرحله, برد/باخت.

---

### Quick demo (win Zombotany level 1)
```
menu enter travel_log_menu
play zombotany 1
plant -t peashooter -l (1, 1)
plant -t peashooter -l (2, 1)
plant -t peashooter -l (3, 1)
plant -t peashooter -l (1, 2)
... (fill columns 1-3 across all 5 rows) ...
tick        (repeat ~40x)
```
Expected: `Kills: 10/10` → `You cleared the plant-headed horde. You win! Reward: 300 coins.`
