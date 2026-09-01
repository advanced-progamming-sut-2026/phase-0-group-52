package view.gui;

import com.badlogic.gdx.math.Rectangle;
import model.ChapterType;
import view.gui.widgets.PamActor;

public final class WorldMapArt {

    public static final String LEVEL_NODE = "768/INITIAL/WORLDMAP/LEVEL_NODE/LEVEL_NODE.PAM";
    public static final String HOLOGRAM =
            "768/INITIAL/WORLDMAP/ZOMBOSS_NODE_HOLOGRAM/ZOMBOSS_NODE_HOLOGRAM.PAM";
    public static final String SPROUT = "768/INITIAL/WORLDMAP/SPROUT/SPROUT.PAM";
    public static final String CONFETTI =
            "768/FULL/UI/LEVELOFTHEDAY/CONFETTI/CONFETTI.PAM";
    public static final String CONFETTI_CLIP = "medium";
    public static final String GIFTBOX =
            "768/INITIAL/WORLDMAP/GIFTBOX_WORLD_MAP/GIFTBOX_WORLD_MAP.PAM";

    public static final String NODE_CLEARED = "finished";
    public static final String NODE_OPEN = "unlocked";
    public static final String NODE_LOCKED = "locked_idle";
    public static final String DANGER_OPEN = "unlocked_idle";
    public static final String DANGER_LOCKED = "locked_idle";
    public static final String ZOMBOSS_OPEN = "active";
    public static final String ZOMBOSS_CLEARED = "defeated";
    public static final String NODE_OPENING = "locked_animation";
    public static final String DANGER_OPENING = "unlocked_animation";

    public static final float MAP_SCALE = 1.34f;

    private static final String WORLDMAP = "768/INITIAL/WORLDMAP/";
    private static final String PAM_ROOT = "assets/pvz/IMAGES/";
    private static final java.util.regex.Pattern ANIM_ID =
            java.util.regex.Pattern.compile("IMAGE_WORLDMAP_([A-Z]+)_ANIM(\\d+)_.*");

    private static final String[] EGYPT_ROLES = {
            "IMAGE_WORLDMAP_EGYPT_ISLAND1",
            "IMAGE_WORLDMAP_EGYPT_ISLAND3",
            "IMAGE_WORLDMAP_EGYPT_ISLAND16"};
    private static final String[] EGYPT_PLATFORMS = {
            "IMAGE_WORLDMAP_EGYPT_ISLAND9",
            "IMAGE_WORLDMAP_EGYPT_ISLAND4",
            "IMAGE_WORLDMAP_EGYPT_ISLAND5"};
    private static final String[] EGYPT_DISTANT = {
            "IMAGE_WORLDMAP_EGYPT_ISLAND26",
            "IMAGE_WORLDMAP_EGYPT_ISLAND24",
            "IMAGE_WORLDMAP_EGYPT_ISLAND25",
            "IMAGE_WORLDMAP_EGYPT_ISLAND35",
            "IMAGE_WORLDMAP_EGYPT_ISLAND34",
            "IMAGE_WORLDMAP_EGYPT_ISLAND36"};
    private static final String[] EGYPT_DECOR = {
            "IMAGE_WORLDMAP_EGYPT_ISLAND37",
            "IMAGE_WORLDMAP_EGYPT_ISLAND21",
            "IMAGE_WORLDMAP_EGYPT_ISLAND18",
            "IMAGE_WORLDMAP_EGYPT_ISLAND17",
            "IMAGE_WORLDMAP_EGYPT_ISLAND20",
            "IMAGE_WORLDMAP_EGYPT_ISLAND19",
            "IMAGE_WORLDMAP_EGYPT_ISLAND36",
            "IMAGE_WORLDMAP_EGYPT_ISLAND34"};
    private static final String[] EGYPT_NEBULAS = {
            "IMAGE_WORLDMAP_EGYPT_ISLAND22",
            "IMAGE_WORLDMAP_EGYPT_ISLAND23"};

    private static final String[] ICEAGE_ROLES = {
            "IMAGE_WORLDMAP_ICEAGE_ANIM3_ANIM3_1307X1318",
            "IMAGE_WORLDMAP_ICEAGE_ISLAND22",
            "IMAGE_WORLDMAP_ICEAGE_ISLAND24"};
    private static final String[] ICEAGE_PLATFORMS = {
            "IMAGE_WORLDMAP_ICEAGE_ANIM12_ANIM12_400X500",
            "IMAGE_WORLDMAP_ICEAGE_ANIM11_ANIM11_400X500",
            "IMAGE_WORLDMAP_ICEAGE_ANIM10_ANIM10_400X500",
            "IMAGE_WORLDMAP_ICEAGE_ANIM26_ANIM26_375X281",
            "IMAGE_WORLDMAP_ICEAGE_ANIM27_ANIM27_301X288"};
    private static final String[] ICEAGE_DISTANT = {
            "IMAGE_WORLDMAP_ICEAGE_ISLAND22",
            "IMAGE_WORLDMAP_ICEAGE_ISLAND23",
            "IMAGE_WORLDMAP_ICEAGE_ISLAND24",
            "IMAGE_WORLDMAP_ICEAGE_ISLAND1"};
    private static final String[] ICEAGE_DECOR = {
            "IMAGE_WORLDMAP_ICEAGE_ANIM15_ANIM15_119X184",
            "IMAGE_WORLDMAP_ICEAGE_ANIM3_ANIM3_115X150",
            "IMAGE_WORLDMAP_ICEAGE_ANIM23_ANIM23_124X118",
            "IMAGE_WORLDMAP_ICEAGE_ANIM16_ANIM16_117X110",
            "IMAGE_WORLDMAP_ICEAGE_ANIM28_ANIM28_271X337"};
    private static final String[] ICEAGE_NEBULAS = {
            "IMAGE_WORLDMAP_ICEAGE_ISLAND43",
            "IMAGE_WORLDMAP_ICEAGE_ISLAND41"};

    private static final String[] DARK_ROLES = {
            "IMAGE_WORLDMAP_DARK_ANIM1_ANIM1_1201X1413",
            "IMAGE_WORLDMAP_DARK_ISLAND21",
            "IMAGE_WORLDMAP_DARK_ANIM22_ANIM22_534X1169"};
    private static final String[] DARK_PLATFORMS = {
            "IMAGE_WORLDMAP_DARK_ISLAND7",
            "IMAGE_WORLDMAP_DARK_ISLAND6",
            "IMAGE_WORLDMAP_DARK_ANIM10_ANIM10_352X358"};
    private static final String[] DARK_DISTANT = {
            "IMAGE_WORLDMAP_DARK_ANIM22_ANIM22_534X1169",
            "IMAGE_WORLDMAP_DARK_ANIM9_ANIM9_373X659",
            "IMAGE_WORLDMAP_DARK_ISLAND21"};
    private static final String[] DARK_DECOR = {
            "IMAGE_WORLDMAP_DARK_ISLAND20",
            "IMAGE_WORLDMAP_DARK_ANIM13_ANIM13_147X111",
            "IMAGE_WORLDMAP_DARK_ANIM9_ANIM9_180X89",
            "IMAGE_WORLDMAP_DARK_ANIM15_ANIM15_102X97"};
    private static final String[] DARK_NEBULAS = {
            "IMAGE_WORLDMAP_DARK_ISLAND53",
            "IMAGE_WORLDMAP_DARK_ISLAND60"};

    private static final String[] BEACH_ROLES = {
            "IMAGE_WORLDMAP_BEACH_ANIM27_ANIM27_1362X953",
            "IMAGE_WORLDMAP_BEACH_ANIM1_ANIM1_283X291",
            "IMAGE_WORLDMAP_BEACH_ISLAND1"};
    private static final String[] BEACH_PLATFORMS = {
            "IMAGE_WORLDMAP_BEACH_ANIM14_ANIM14_358X512",
            "IMAGE_WORLDMAP_BEACH_ANIM13_ANIM13_397X399",
            "IMAGE_WORLDMAP_BEACH_ANIM15_ANIM15_325X443",
            "IMAGE_WORLDMAP_BEACH_ANIM12_ANIM12_335X420",
            "IMAGE_WORLDMAP_BEACH_ANIM16_ANIM16_339X318",
            "IMAGE_WORLDMAP_BEACH_ANIM11_ANIM11_297X281",
            "IMAGE_WORLDMAP_BEACH_ANIM17_ANIM17_321X255",
            "IMAGE_WORLDMAP_BEACH_ANIM10_ANIM10_295X271"};
    private static final String[] BEACH_DISTANT = {
            "IMAGE_WORLDMAP_BEACH_ISLAND24",
            "IMAGE_WORLDMAP_BEACH_ISLAND22",
            "IMAGE_WORLDMAP_BEACH_ISLAND23"};
    private static final String[] BEACH_DECOR = {
            "IMAGE_WORLDMAP_BEACH_ISLAND13",
            "IMAGE_WORLDMAP_BEACH_ISLAND14",
            "IMAGE_WORLDMAP_BEACH_ISLAND16",
            "IMAGE_WORLDMAP_BEACH_ISLAND15",
            "IMAGE_WORLDMAP_BEACH_ANIM8_ANIM8_91X367",
            "IMAGE_WORLDMAP_BEACH_ANIM20_ANIM20_233X132"};
    private static final String[] BEACH_NEBULAS = {
            "IMAGE_WORLDMAP_BEACH_ISLAND41",
            "IMAGE_WORLDMAP_BEACH_ISLAND42"};

    private static final int[] EGYPT_ANIMS = {};

    private static final int[] ICEAGE_ANIMS =
            {13, 14, 15, 16, 17, 22, 23, 28};

    private static final int[] DARK_ANIMS =
            {1, 3, 4, 5, 6, 7, 9, 10, 12, 13, 14, 15, 16, 22};

    private static final int[] BEACH_ANIMS =
            {1, 4, 5, 6, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 27, 32, 35};

    private WorldMapArt() {}

    private static ChapterType safe(ChapterType chapter) {
        return chapter == null ? ChapterType.ANCIENT_EGYPT : chapter;
    }

    private static String[] roles(ChapterType chapter) {
        switch (safe(chapter)) {
            case FROSTBITE_CAVES: return ICEAGE_ROLES;
            case DARK_AGES:       return DARK_ROLES;
            case BIG_WAVE_BEACH:  return BEACH_ROLES;
            default:              return EGYPT_ROLES;
        }
    }

    public static String entryIsland(ChapterType chapter) {
        return roles(chapter)[0];
    }

    public static String statueIsland(ChapterType chapter) {
        return roles(chapter)[1];
    }

    public static String faceIsland(ChapterType chapter) {
        return roles(chapter)[2];
    }

    public static String[] platforms(ChapterType chapter) {
        switch (safe(chapter)) {
            case FROSTBITE_CAVES: return ICEAGE_PLATFORMS.clone();
            case DARK_AGES:       return DARK_PLATFORMS.clone();
            case BIG_WAVE_BEACH:  return BEACH_PLATFORMS.clone();
            default:              return EGYPT_PLATFORMS.clone();
        }
    }

    public static String[] distant(ChapterType chapter) {
        switch (safe(chapter)) {
            case FROSTBITE_CAVES: return ICEAGE_DISTANT.clone();
            case DARK_AGES:       return DARK_DISTANT.clone();
            case BIG_WAVE_BEACH:  return BEACH_DISTANT.clone();
            default:              return EGYPT_DISTANT.clone();
        }
    }

    public static String[] decor(ChapterType chapter) {
        switch (safe(chapter)) {
            case FROSTBITE_CAVES: return ICEAGE_DECOR.clone();
            case DARK_AGES:       return DARK_DECOR.clone();
            case BIG_WAVE_BEACH:  return BEACH_DECOR.clone();
            default:              return EGYPT_DECOR.clone();
        }
    }

    public static String[] nebulas(ChapterType chapter) {
        switch (safe(chapter)) {
            case FROSTBITE_CAVES: return ICEAGE_NEBULAS.clone();
            case DARK_AGES:       return DARK_NEBULAS.clone();
            case BIG_WAVE_BEACH:  return BEACH_NEBULAS.clone();
            default:              return EGYPT_NEBULAS.clone();
        }
    }

    public static int[] anims(ChapterType chapter) {
        switch (safe(chapter)) {
            case FROSTBITE_CAVES: return ICEAGE_ANIMS.clone();
            case DARK_AGES:       return DARK_ANIMS.clone();
            case BIG_WAVE_BEACH:  return BEACH_ANIMS.clone();
            default:              return EGYPT_ANIMS.clone();
        }
    }

    public static String animOf(ChapterType chapter, String imageId) {
        if (imageId == null) {
            return null;
        }
        java.util.regex.Matcher found = ANIM_ID.matcher(imageId);
        if (!found.matches()) {
            return null;
        }
        String path = resolve("768/FULL/WORLDMAP/" + found.group(1) + "/ANIM"
                + found.group(2) + "/ANIM" + found.group(2) + ".PAM");
        return exists(path) ? path : null;
    }

    public static String animPath(ChapterType chapter, int number) {
        String world = ChapterArt.world(safe(chapter));
        return resolve("768/FULL/WORLDMAP/" + world + "/ANIM" + number
                + "/ANIM" + number + ".PAM");
    }

    public static String bossSkull(ChapterType chapter, boolean unlocked) {
        return unlocked
                ? "IMAGE_WORLDMAP_LEVEL_NODE_GARGANTUAR_LEVEL_NODE_GARGANTUAR_86X57"
                : "IMAGE_WORLDMAP_LEVEL_NODE_GARGANTUAR_LEVEL_NODE_GARGANTUAR_84X55";
    }

    public static String pathBeam(ChapterType chapter) {
        return "IMAGE_WORLDMAP_MAP_PATH_MAP_PATH_135X16";
    }

    public static String pathGlow(ChapterType chapter) {
        return "IMAGE_WORLDMAP_MAP_PATH_MAP_PATH_87X87";
    }

    public static String dangerNode(ChapterType chapter) {
        return rig(WORLDMAP, "DANGER_NODE_" + ChapterArt.world(safe(chapter)));
    }

    public static String zombossNode(ChapterType chapter) {
        return rig(WORLDMAP, "ZOMBOSS_NODE_" + ChapterArt.world(safe(chapter)));
    }

    public static String pinata(ChapterType chapter) {
        return rig("768/INITIAL/EFFECTS/", "PRIZE_PINATA_" + ChapterArt.world(safe(chapter)));
    }

    public static String island(ChapterType chapter, int number) {
        return "IMAGE_WORLDMAP_" + ChapterArt.world(safe(chapter)) + "_ISLAND" + number;
    }

    public static String trophy(ChapterType chapter) {
        return "IMAGE_ENDLEVEL_" + ChapterArt.world(safe(chapter)) + "_TROPHY";
    }

    public static boolean hasClip(Assets assets, String path, String clip) {
        if (assets == null || path == null || clip == null || !assets.load(path)) {
            return false;
        }
        java.util.List<String> clips = assets.player().clips(path);
        return clips != null && clips.contains(clip);
    }

    public static String clipOr(Assets assets, String path, String wanted, String fallback) {
        return hasClip(assets, path, wanted) ? wanted : fallback;
    }

    public static PamActor rigged(Assets assets, String path, String clip, String playing) {
        if (!exists(path)) {
            return new PamActor(assets, path, clip);
        }
        String safeClip = firstClip(assets, path, clip);
        PamActor actor = new PamActor(assets, path, safeClip);
        Rectangle box = assets == null ? null : assets.player().bounds(path, safeClip);
        if (box != null) {
            actor.setExtent(box.x, -(box.y + box.height), box.width, box.height);
        }
        actor.setFit(true);
        String wanted = firstClip(assets, path, playing);
        if (wanted != null && !wanted.equals(safeClip)) {
            actor.play(wanted, true, null);
        }
        return actor;
    }

    private static String firstClip(Assets assets, String path, String clip) {
        if (assets == null || path == null || !assets.load(path)) {
            return clip;
        }
        java.util.List<String> clips = assets.player().clips(path);
        if (clips == null || clips.isEmpty()) {
            return clip;
        }
        return clips.contains(clip) ? clip : clips.get(0);
    }

    private static String rig(String folder, String name) {
        return resolve(folder + name + "/" + name + ".PAM");
    }

    private static String resolve(String path) {
        if (exists(path)) {
            return path;
        }
        String swapped = path.contains("/INITIAL/")
                ? path.replace("/INITIAL/", "/FULL/") : path.replace("/FULL/", "/INITIAL/");
        return exists(swapped) ? swapped : path;
    }

    private static boolean exists(String path) {
        return com.badlogic.gdx.Gdx.files != null
                && com.badlogic.gdx.Gdx.files.local(PAM_ROOT + path).exists();
    }
}
