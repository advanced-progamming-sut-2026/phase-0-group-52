# Art libraries

Two third-party libraries by [pizpizi](https://github.com/pizpizi) supply the game's
look. Both are MIT licensed and pulled from JitPack, so **no asset files are
committed to this repository** — Gradle downloads them.

```groovy
repositories { maven { url 'https://jitpack.io' } }

implementation "com.github.pizpizi:pvz-skin:v0.1.1"
implementation "com.github.pizpizi:libPVZ:v0.1.6"
```

## pvz-skin — in use

<https://github.com/pizpizi/pvz-skin>

A Scene2D UI skin built from Plants vs. Zombies 2 interface art: dialog frames,
button faces, scrollbars, text fields, progress meters and card backings. The
atlas, JSON and fonts all live inside the jar, so `PvzSkin.get()` works with no
filesystem paths.

`UiKit` loads it once at startup and maps its drawables onto this project's style
names, so screens did not have to change:

| Project style | Skin drawable |
|---|---|
| `panel` | the skin's `BorderedTable` (`dialogborder` over `inner_bkgd`) |
| `sunken` | `dialog_asset_inner_bkgd` |
| `card` | `cards_almanac_plant_card` |
| `questPanel` | `quests_panel_edge_to_edge` |
| `nameField` | `mainmenu_name_field` |
| `primary` / `default` | `green` button |
| `secondary` | `brown` button |
| `danger` | `purple` button |
| `info` | `default` (blue) button |
| text fields | `mainmenu_text_entry_field` |
| scrollbars | `almanac_general_scrollbar` |

Icon buttons are mapped centrally in `Icons.java`, each as a `normal`/`selected`
region pair so hovering swaps to the art's own highlighted state:

| Role | Region |
|---|---|
| Back | `almanac_buttons_hud_back` |
| Settings | `hud_settingsbutton_buttons_hud_settings` |
| Almanac | `hud_almanacbutton_buttons_hud_almanac` |
| News | `hud_tasklist_buttons_hud_task_list` |
| Quests | `generic_buttons_hud_quests` |
| Minigames | `generic_button_hud_minigames` |
| Greenhouse | `generic_buttons_hud_zg` |
| Quit the game | `generic_close_btn` (square) |
| Close a popup | `generic_close_circle` (round) |

### Fonts come from the skin

The skin bundles the real game faces as FreeType fonts, so the project uses those
rather than loading its own:

| Role | Skin font | Notes |
|---|---|---|
| Subtext | `BRIANNETOD` | Brianne's Hand, the almanac face |
| Body | `FBUSV8C6EI_3` | 16px Burbank |
| Titles | `FBUSV8C5EI_2` | 24px Burbank |
| Titles on dark | `FBUSV8C5EI_2_outline` | 30px Burbank with a 2px outline |
| Huge | `FBUSV8C5EI_1_outline` | 40px Burbank with a 3px outline |
| Buttons | `HOUSE_OF_TERROR` | what the skin's own button styles use |

The outlined variants are what give headings the authentic look on dark
backgrounds, and using the skin's fonts also fixes button labels sitting off
centre, since these faces carry the metrics the button art was drawn around.

`FontFactory` and `assets/fonts/` remain only as a fallback for when the skin
cannot load.

### Panels use the skin's BorderedTable

`ui.panel()` returns the skin's own `BorderedTable`, which draws the fill inset
and the border deliberately oversized and offset (`-5, -9, +10, +10`). An earlier
attempt to compose the two drawables with a symmetric inset left artefacts along
the bottom edge of every panel; the skin's own class has the offsets right.

**Fallback.** If the skin fails to load for any reason, `UiKit` logs a warning and
falls back to the shapes `Primitives` draws at runtime, so the game still starts.
`UiKit.hasArtSkin()` reports which path is active.

It brings `gdx-freetype` and TenPatch transitively; the matching
`gdx-freetype-platform` natives are declared here because the skin only lists them
for its own tests.

## libPVZ — wired, not yet usable

<https://github.com/pizpizi/libPVZ>

Parses and renders PopCap PAM animation files — the format the real game's plants
and zombies are animated in. This is what would eventually replace the coloured
placeholder discs with actual animated sprites.

**It cannot do anything yet.** It needs PAM files and textures extracted from the
game's OBB, and those are copyrighted EA assets that must not be committed here.
The dependency is declared so the wiring is ready; once someone supplies extracted
assets locally (kept out of version control, like the fonts), a `TextureBank` and
`PamPlayer` can be pointed at them.

Until then the placeholder art in `Primitives` and `Carousel` stands in.
