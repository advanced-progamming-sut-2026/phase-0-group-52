# Test Cases — All Plants (68)

**Common setup** (deck-free quick test: with no chosen deck you may plant anything):
```
menu enter chapter_menu
menu enter chapter -c ANCIENT_EGYPT -l 1
start special -t deadline        # a level with waves; or use a normal level after choosing a deck
cheat sun 5000
cheat remove-cooldown
```
Plant: `plant plant -t <name> -l (<x>, <y>)`  ·  test a target with `cheat spawn-zombie -t default -l (9, <y>)` then `tick <n>`.
Give plant food: `cheat add-plant-food` then `feed plant -l (<x>, <y>)`.

> Tip: `-t` accepts spaces or `-`/`_` (e.g. `-t Wall-nut`, `-t wall_nut`, `-t "Snow Pea"`).

---

## SUN_PRODUCER (produce sun on the ground; collect with `collect sun`)
| Plant | Expect |
|---|---|
| Sunflower | produces 50 sun every ~7 s → `produced a sun at …` |
| Twin Sunflower | produces 100 sun |
| Sun-shroom | small→big, produces 25 |
| Primal Sunflower | produces 75 |
| Gold Bloom | instant large sun burst (plant-food-like) |
| Enlighten-mint (mint) | boosts/plant-food to its family |

## SHOOTER (shoot the frontmost zombie in the row)
| Plant | Expect |
|---|---|
| Peashooter | 20 dmg per shot |
| Repeater | 2 peas (double dmg) |
| Threepeater | shoots its row **and both adjacent rows** |
| Snow Pea | damages **and slows** the zombie; plant-food freezes the lane |
| Rotobaga | fires along **4 diagonals** |
| Pea Pod | stackable heads (re-plant to add a head, up to 5) |
| Split Pea | shoots **forward and backward** (2× behind) |
| Citron | big charged shot (800 dmg, slow rate) |
| Bowling Bulb | bouncing bulb, ramps damage |
| Fire Peashooter | 2× fire damage (Torchwood-like) |
| Starfruit | fires in **5 directions** |
| Goo Peashooter | direct damage (ignores armor) |
| Mega Gatling Pea | rapid pea barrage |
| Sea-shroom | water shooter |
| Puff-shroom | short-range, cheap, limited lifespan (60 s) |
| Appease-mint (mint) | shooter-family mint |

## HOMING (target the nearest zombie anywhere)
| Plant | Expect |
|---|---|
| Caulipower | hypnotizes the nearest zombie |
| Electric Blueberry | ~5000 dmg → **destroys** a zombie (even Gargantuar) |
| Magnet-shroom | **strips armor** off the nearest armored zombie → `stripped the armor…` |
| Cat-tail | homing shots at nearest zombie (any lane) |
| catTail-mint (mint) | homing-family mint |

## STRIKE_THROUGH (pierce a line of zombies)
| Plant | Expect |
|---|---|
| Cactus | pierces up to **3** zombies in the row |
| Fume-shroom | short-range fume that passes through several |
| Pierce-mint (mint) | pierce-family mint |

## LOBBER (arc over obstacles, hit the frontmost)
| Plant | Expect |
|---|---|
| Cabbage-pult | basic lob |
| Kernel-pult | lob (sometimes butter → slow) |
| Melon-pult | heavy AoE lob (splash) |
| Winter Melon | AoE lob **+ slows** (ICE) |
| Pepper-pult | fire lob (melts ice tiles) |
| Arma-mint (mint) | lobber-family mint |

## EXPLOSIVE (one-shot area effects)
| Plant | Expect |
|---|---|
| Potato Mine | arms after ~15 s, then explodes on contact |
| Primal Potato Mine | arms faster, bigger blast |
| Cherry Bomb | instant **3×3** blast on plant |
| Squash | crushes zombie(s) on its tile |
| Grapeshot | scatter of bouncing shots |
| Jalapeno | **clears the whole row** instantly |
| Doom-shroom | huge blast, leaves an unplantable **crater** |
| Tangle Kelp | drags a water zombie under |
| Iceberg Lettuce | freezes a zombie on its tile |
| Ice-shroom | **freezes all** zombies on screen |
| Hot Potato | thaws a frozen tile / plant |
| Grave Buster | on plant on a tombstone → **removes the tombstone** |
| Bombard-mint (mint) | explosive-family mint |

## MELEE (hit adjacent zombies, front & back)
| Plant | Expect |
|---|---|
| Bonk Choy | punches the tile in front (and behind) |
| Phat Beet | melee AoE around it |
| Chomper | **swallows** a zombie whole, then digests ~40 s (vulnerable) |
| Wasabi Whip | fiery melee (melts ice) |
| Kiwibeast | damage ramps up over time |
| Enforce-mint (mint) | melee-family mint |

## WALL_NUT (defense / utility)
| Plant | Expect |
|---|---|
| Wall-nut | 4000 HP wall (blocks) |
| Tall-nut | tall wall (blocks jumpers) |
| Endurian | wall that damages attackers (spikes) |
| Garlic | diverts a zombie to an adjacent lane |
| Sweet Potato | lures/pulls zombies |
| Explode-o-nut | wall that explodes when destroyed |
| Pumpkin | shell around another plant |
| Sun Bean | zombie that eats it takes sun-based damage |
| Reinforce-mint (mint) | defense-family mint |

## MODIFIER (passive / support)
| Plant | Expect |
|---|---|
| Torchwood | passing peas become **fire** (2× dmg) — verify with a Peashooter behind it |
| Hypno-shroom | zombie that eats it turns to **fight for you** |
| Imitater | copies the next plant you place |
| Lily Pad | base tile for water plants |
| Enchant-mint (mint) | modifier-family mint |

---

## Upgrade & plant-food (any plant)
```
menu enter collection_menu
menu collection purchase-plant -p Peashooter    # x5 → 5 seed packets
menu collection upgrade-plant -p Peashooter     # level 2: +10 damage
```
Levels 2/3/4 apply damage / HP / speed(action-interval) / recharge / cost buffs from `plants.json`.
Plant food in a level: `cheat add-plant-food` → `feed plant -l (x, y)` → each plant's special plant-food effect.
