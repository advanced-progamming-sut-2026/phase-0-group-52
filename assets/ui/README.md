# Interface art

Single images pulled from the game that the `pvz-skin` library does not carry.

| File | Source |
|---|---|
| `pvz2_logo_horizontal.png` | `IMAGE_UI_MAINMENU_PVZ2_LOGO_HORIZONTAL`, 402x68 |
| `leaderboard.png` | `IMAGE_UI_GAMECENTER_ANDROID_ACHIEVEMENTS`, 94x70 |
| `leaderboard_selected.png` | `IMAGE_UI_GAMECENTER_ANDROID_ACHIEVEMENTS_SELECT`, 94x70 |

The two leaderboard icons are cropped from `ATLASES/UI_GAMECENTER_768_00.PNG` at
(91, 1) and (91, 73), the coordinates `RESOURCES.json` records for them. They
drive the player list's header button through `Icons.LEADERBOARD`. A 1536
version exists in `RESOURCES.json` at 188x139 but its atlas is not in
`pvz-assets`.

The logo is not currently drawn anywhere: every title backdrop already has one
painted into the artwork, at a different position and size in each. It is kept
here for a screen that needs a standalone logo later.

Only the 768 variant exists in `pvz-assets`. `RESOURCES.json` also lists a 1536
version at 804x136, which would be needed for a genuinely sharper logo.
