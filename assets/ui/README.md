# Interface art

Single images pulled from the game that the `pvz-skin` library does not carry.

| File | Source |
|---|---|
| `leaderboard.png` | `IMAGE_UI_GAMECENTER_ANDROID_ACHIEVEMENTS`, 94x70 |
| `leaderboard_selected.png` | `IMAGE_UI_GAMECENTER_ANDROID_ACHIEVEMENTS_SELECT`, 94x70 |
| `currency_plus.png` | green plus badge cut out of the skin's `image_ui_generic_button_generic_currency_normal`, 32x32 |

| `scroll_mid.png` | `IMAGE_UI_JOUST_LEADERBOARD_LEADERBOARD_SCROLL_TOP` / `_MID` |
| `rank_standstill.png`, `rank_demoted.png` | joust leaderboard rank banners, reused for 1st, 2nd and 3rd |

The scroll pieces are cropped from `ATLASES/UI_JOUST_LEADERBOARD_768_00.PNG` at the
coordinates in `RESOURCES.json`, then upscaled 5x. Only `scroll_mid` is drawn: the
leaderboard butts the parchment against the top bar and runs it off the bottom of the
screen, so neither the rolled top nor the wooden base is ever visible.

`currency_plus.png` exists because that skin region bakes the badge into a
158x59 pill whose stretchable band runs straight through it, so any nine-patch
that resized the pill squashed the badge. Cut out as its own image it keeps a
fixed aspect at any wallet size.

The two leaderboard icons are cropped from `ATLASES/UI_GAMECENTER_768_00.PNG` at
(91, 1) and (91, 73), the coordinates `RESOURCES.json` records for them. They
drive the player list's header button through `Icons.LEADERBOARD`. A 1536
version exists in `RESOURCES.json` at 188x139 but its atlas is not in
`pvz-assets`.

Every file here is loaded by the code. `pvz-assets` only ships 768 atlases, so
anything drawn larger than its source is upscaled first (Upscayl,
realesrgan-x4plus-anime) and the upscaled copy is what lives here.
