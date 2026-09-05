package view.gui;

import view.gui.widgets.PamActor;

public final class FrostArt {

    public static final String TILE_ICE =
            "IMAGE_EFFECTS_ZOMBONI_TILE_ICE_ZOMBONI_TILE_ICE_133X157";
    public static final String ICE_PLANT =
            "768/FULL/EFFECTS/FROSTBITE_ICE_BLOCK_PLANT/FROSTBITE_ICE_BLOCK_PLANT.PAM";
    public static final String ICE_ZOMBIE =
            "768/FULL/EFFECTS/FROSTBITE_ICE_BLOCK_ZOMBIE/FROSTBITE_ICE_BLOCK_ZOMBIE.PAM";
    public static final String ICE_PARTICLES =
            "768/FULL/EFFECTS/FROSTBITE_ICE_BLOCK_PARTICLES/FROSTBITE_ICE_BLOCK_PARTICLES.PAM";
    public static final String CHILL_WIND =
            "768/FULL/EFFECTS/FROSTBITE_CHILL_WIND/FROSTBITE_CHILL_WIND.PAM";

    public static final String TILE_KEY = "ice|tile";
    public static final String BLOCK_KEY = "ice|block";
    public static final String WIND_KEY = "ice|wind";

    public static final float BLOCK_SPAN = 1.35f;

    public static final String PLANT_BLOCK_KEY = "ice|plant";
    public static final String ZOMBIE_BLOCK_KEY = "ice|zombie";

    private FrostArt() {
    }

    public static PamActor rig(Assets assets, String path, String... wanted) {
        if (assets == null || !assets.load(path)) {
            return null;
        }
        String clip = clipOf(assets, path, wanted);
        if (clip == null) {
            return null;
        }
        PamActor actor = new PamActor(assets, path, clip).setFit(true);
        return actor.isReady() ? actor : null;
    }

    public static String clipOf(Assets assets, String path, String... wanted) {
        java.util.List<String> clips = assets.player().clips(path);
        if (clips == null || clips.isEmpty()) {
            return null;
        }
        for (String name : wanted) {
            if (clips.contains(name)) {
                return name;
            }
        }
        return clips.get(0);
    }
}
