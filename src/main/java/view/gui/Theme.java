package view.gui;

import com.badlogic.gdx.graphics.Color;

/**
 * Colour and metric constants for the graphical shell.
 *
 * <p>The palette imitates Plants vs. Zombies 2: warm parchment panels with thick
 * brown outlines, a bright lawn green for primary actions, and a sun yellow used
 * only for currency and highlights. Everything the screens draw comes from here,
 * so re-skinning later means editing this file rather than hunting literals.
 */
public final class Theme {

    private Theme() {
    }

    // -- surfaces -----------------------------------------------------------

    /** Deep background behind every screen. */
    public static final Color BACKDROP = rgb(0x2E4A1E);
    /** Slightly lighter backdrop band, used for striped lawns. */
    public static final Color BACKDROP_ALT = rgb(0x365627);
    /** Parchment panel face. */
    public static final Color PANEL = rgb(0xE8D5A9);
    /** A dimmer parchment for insets and list rows. */
    public static final Color PANEL_SUNKEN = rgb(0xD6BF8E);
    /** Panel outline. */
    public static final Color OUTLINE = rgb(0x6B4423);
    /** Outline for lighter, secondary elements. */
    public static final Color OUTLINE_SOFT = rgb(0x8B5A2B);
    /** Dim scrim drawn behind modal dialogs. */
    public static final Color SCRIM = new Color(0f, 0f, 0f, 0.55f);

    // -- text ---------------------------------------------------------------

    public static final Color TEXT = rgb(0x3E2723);
    public static final Color TEXT_MUTED = rgb(0x7A5C46);
    public static final Color TEXT_ON_DARK = rgb(0xF5ECD7);
    public static final Color TEXT_DISABLED = rgb(0x9E8A72);

    // -- actions ------------------------------------------------------------

    public static final Color GREEN = rgb(0x7CB342);
    public static final Color GREEN_LIGHT = rgb(0x9CCC54);
    public static final Color GREEN_DARK = rgb(0x558B2F);
    public static final Color RED = rgb(0xC62828);
    public static final Color RED_LIGHT = rgb(0xE04B4B);
    public static final Color BLUE = rgb(0x4A7EBB);
    public static final Color BLUE_LIGHT = rgb(0x6699D6);

    // -- resources ----------------------------------------------------------

    public static final Color SUN = rgb(0xFFD23F);
    public static final Color SUN_DEEP = rgb(0xF5A623);
    public static final Color COIN = rgb(0xE8B833);
    public static final Color GEM = rgb(0x36C4D9);
    public static final Color PLANT_FOOD = rgb(0x66BB6A);

    // -- state --------------------------------------------------------------

    public static final Color LOCKED = rgb(0x6D6459);
    public static final Color BOOSTED = rgb(0xFFC107);
    public static final Color SELECTED = rgb(0x4CAF50);

    // -- metrics ------------------------------------------------------------

    /** Virtual resolution; the viewport letterboxes to preserve aspect. */
    public static final int WORLD_WIDTH = 1280;
    public static final int WORLD_HEIGHT = 720;

    public static final int PAD = 10;
    public static final int PAD_SMALL = 6;
    public static final int PAD_LARGE = 18;
    public static final int RADIUS = 10;
    public static final int BORDER = 3;

    /** Seed packet dimensions, shared by collection, choose-plant and the lawn HUD. */
    public static final int PACKET_WIDTH = 98;
    public static final int PACKET_HEIGHT = 132;

    public static final float TRANSITION_TIME = 0.22f;

    /**
     * Per-family colour for plant placeholders. Real art replaces the coloured
     * disc later; until then the family is at least readable at a glance.
     */
    public static Color plantFamily(String categoryName) {
        if (categoryName == null) {
            return rgb(0x7CB342);
        }
        if (categoryName.equals("SUN_PRODUCER")) return rgb(0xF7C948);
        if (categoryName.equals("SHOOTER")) return rgb(0x62A744);
        if (categoryName.equals("HOMING")) return rgb(0x7E57C2);
        if (categoryName.equals("STRIKE_THROUGH")) return rgb(0x26A69A);
        if (categoryName.equals("LOBBER")) return rgb(0xEF7043);
        if (categoryName.equals("EXPLOSIVE")) return rgb(0xD84315);
        if (categoryName.equals("MELEE")) return rgb(0xAD1457);
        if (categoryName.equals("MODIFIER")) return rgb(0x5C6BC0);
        if (categoryName.equals("MINT")) return rgb(0x4DB6AC);
        if (categoryName.equals("WALL_NUT")) return rgb(0xA1887F);
        return rgb(0x7CB342);
    }

    /** Per-chapter accent, used on adventure cards and level backgrounds. */
    public static Color chapter(String chapterName) {
        if (chapterName == null) {
            return GREEN;
        }
        if (chapterName.equals("ANCIENT_EGYPT")) return rgb(0xD4A537);
        if (chapterName.equals("FROSTBITE_CAVES")) return rgb(0x6EC6E8);
        if (chapterName.equals("BIG_WAVE_BEACH")) return rgb(0x2E9BC6);
        if (chapterName.equals("DARK_AGES")) return rgb(0x6E5A8C);
        return GREEN;
    }

    private static Color rgb(int hex) {
        return new Color(
                ((hex >> 16) & 0xFF) / 255f,
                ((hex >> 8) & 0xFF) / 255f,
                (hex & 0xFF) / 255f,
                1f);
    }

    /** Returns a copy of {@code base} with its alpha replaced. */
    public static Color alpha(Color base, float alpha) {
        return new Color(base.r, base.g, base.b, alpha);
    }

    /** Blends {@code base} toward white by {@code amount} in 0..1. */
    public static Color lighten(Color base, float amount) {
        return new Color(
                base.r + (1f - base.r) * amount,
                base.g + (1f - base.g) * amount,
                base.b + (1f - base.b) * amount,
                base.a);
    }

    /** Blends {@code base} toward black by {@code amount} in 0..1. */
    public static Color darken(Color base, float amount) {
        return new Color(base.r * (1f - amount), base.g * (1f - amount), base.b * (1f - amount), base.a);
    }
}
