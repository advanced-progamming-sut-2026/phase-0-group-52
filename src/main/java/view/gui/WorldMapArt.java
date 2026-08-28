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

    private static final int EGYPT_ENTRY = 1;
    private static final int EGYPT_STATUE = 3;
    private static final int EGYPT_FACE = 16;
    private static final int[] EGYPT_PLATFORMS = {9, 4, 5};
    private static final int[] EGYPT_NEBULAS = {22, 23};
    private static final int[] EGYPT_DISTANT = {26, 24, 25, 35, 34, 36};
    private static final int[] EGYPT_DECOR = {37, 21, 18, 17, 20, 19, 36, 34};

    private WorldMapArt() {}

    public static int entryIsland(ChapterType chapter) {
        return EGYPT_ENTRY;
    }

    public static int statueIsland(ChapterType chapter) {
        return EGYPT_STATUE;
    }

    public static int faceIsland(ChapterType chapter) {
        return EGYPT_FACE;
    }

    public static int[] platforms(ChapterType chapter) {
        return EGYPT_PLATFORMS.clone();
    }

    public static int[] nebulas(ChapterType chapter) {
        return EGYPT_NEBULAS.clone();
    }

    public static int[] decor(ChapterType chapter) {
        return EGYPT_DECOR.clone();
    }

    public static int[] distant(ChapterType chapter) {
        return EGYPT_DISTANT.clone();
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
        return rig(WORLDMAP, "DANGER_NODE_" + ChapterArt.world(chapter));
    }

    public static String zombossNode(ChapterType chapter) {
        return rig(WORLDMAP, "ZOMBOSS_NODE_" + ChapterArt.world(chapter));
    }

    public static String pinata(ChapterType chapter) {
        return rig("768/INITIAL/EFFECTS/", "PRIZE_PINATA_"
                + ChapterArt.world(chapter == null ? ChapterType.ANCIENT_EGYPT : chapter));
    }

    public static String island(ChapterType chapter, int number) {
        return "IMAGE_WORLDMAP_" + ChapterArt.world(chapter) + "_ISLAND" + number;
    }

    public static String trophy(ChapterType chapter) {
        return "IMAGE_ENDLEVEL_" + ChapterArt.world(chapter) + "_TROPHY";
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
        String safe = firstClip(assets, path, clip);
        PamActor actor = new PamActor(assets, path, safe);
        Rectangle box = assets == null ? null : assets.player().bounds(path, safe);
        if (box != null) {
            actor.setExtent(box.x, -(box.y + box.height), box.width, box.height);
        }
        actor.setFit(true);
        String wanted = firstClip(assets, path, playing);
        if (wanted != null && !wanted.equals(safe)) {
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
        return folder + name + "/" + name + ".PAM";
    }
}
