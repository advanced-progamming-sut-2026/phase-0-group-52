# Game font

Drop a `.ttf` or `.otf` file in this folder and the game uses it for every label
on the next launch. No code change and no rebuild is needed — the first font file
found here (alphabetically) wins. Installing the font system-wide works too.

If neither is present, the game falls back to the closest installed system face.

## The two fonts this project uses

The interface uses two faces, matched by family name. Put both files in this
folder and `FontFactory` assigns them automatically:

| Role | Font | File |
|---|---|---|
| Headings, buttons, labels | **Burbank Big Condensed Bold** | `Burbank Big Condensed Bold.otf` |
| Subtext, captions, stats | **Brianne's Hand** | `Brianne_s_hand.ttf` |

Burbank Big Condensed Bold is the Plants vs. Zombies wordmark face, sold by
House Industries. Brianne's Hand, by Jeni Hopewell, is the community's match for
the almanac text (listed in font rips as `BrianneTod`) and comes from
<https://www.dafont.com/briannes-hand.font>.

Matching is by the family name the font reports, not the filename — Burbank
reports itself as `Burbank Big Cd Bd`, which is covered. If a font is missing the
game falls back to the closest installed face and says so in the log.

## Licence — read before committing anything here

**Neither font may be committed to this repository.**

- **Burbank Big Condensed Bold** is a commercial House Industries typeface. The
  university holds a licence to *use* it; that does not grant redistribution
  rights, and pushing it to a public GitHub repo would be redistribution.
- **Brianne's Hand** is free for personal use only, which likewise grants no
  redistribution rights.

The `.gitignore` beside this file excludes `*.ttf`, `*.otf` and `*.ttc` for
exactly that reason. Each person adds the fonts to their own clone.
