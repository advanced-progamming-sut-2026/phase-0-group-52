package view.gui;

import model.entities.Projectile;
import model.entities.plants.PlantData;
import model.entities.plants.PlantRecord;
import model.entities.plants.Muzzle;
import model.entities.plants.Plants;
import view.gui.widgets.PamActor;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ShotArt {

    public static final String FED = Projectile.FED;
    public static final String BULB = Projectile.BULB;

    private static final float CANVAS = 120f;
    private static final float CELL_SHARE = 0.95f;
    private static final String FX = "768/INITIAL/EFFECTS/";

    private static final String PEA = FX + "T_PEA_PROJECTILE/T_PEA_PROJECTILE.PAM";
    private static final String SPLAT_PEA = FX + "T_SPLAT_PEA/T_SPLAT_PEA.PAM";
    private static final String SPLAT_GIANT = FX + "SPLAT_GIANTPEA/SPLAT_GIANTPEA.PAM";

    private static final Map<Plants, String> OWN_SHOT = new HashMap<Plants, String>();
    private static final Map<Plants, String> OWN_SPLAT = new HashMap<Plants, String>();

    static {
        OWN_SHOT.put(Plants.SNOW_PEA, FX + "T_SNOW_PEA/T_SNOW_PEA.PAM");
        OWN_SHOT.put(Plants.FIRE_PEASHOOTER, FX + "T_FIRE_PEA/T_FIRE_PEA.PAM");
        OWN_SPLAT.put(Plants.SNOW_PEA, FX + "T_SPLAT_SNOW_PEA/T_SPLAT_SNOW_PEA.PAM");
        OWN_SPLAT.put(Plants.FIRE_PEASHOOTER, FX + "T_SPLAT_FIRE_PEA/T_SPLAT_FIRE_PEA.PAM");
    }

    private static final String[] SHOT_KEYS = {
        "T_PROJECTILE", "T_PROJECTILE1", "PROJECTILE", "PROJECTILE1", "PROJECTILES",
        "T_CITRUS_ORB", "CITRUS_ORB",
    };
    private static final String[] FED_KEYS = {
        "PLANTFOOD_GIANTPEA", "PLANTFOOD_PROJECTILE", "PROJECTILE_PLANTFOOD",
        "PLANTFOOD_ORB",
    };
    private static final String[] LOB_SPLAT_KEYS = {
        "T_SPLAT", "SPLAT", "T_PROJECTILE_SPLAT", "PROJECTILE_SPLAT",
    };
    private static final String[] SPLAT_KEYS = {
        "T_PROJECTILE_HIT", "PROJECTILE_HIT", "T_CITRUS_ORB_HIT", "CITRUS_ORB_HIT", "T_HIT",
    };
    private static final String[] FED_SPLAT_KEYS = {
        "PLANTFOOD_ORB_HIT", "PLANTFOOD_HIT",
    };

    private ShotArt() {
    }

    public static float baseScale(Projectile.Kind kind) {
        return CELL_SHARE;
    }

    public static String rig(Plants source, Projectile.Kind kind) {
        return rig(source, kind, "");
    }

    public static String rig(Plants source, Projectile.Kind kind, String variant) {
        if (variant != null && variant.startsWith(BULB)) {
            String numbered = fromRecord(source,
                    new String[] {"PROJECTILE" + variant.substring(BULB.length())});
            if (numbered != null) {
                return numbered;
            }
        }
        if (variant != null && variant.startsWith(FED)) {
            String fed = fromRecord(source, FED_KEYS);
            if (fed == null && source == Plants.GOO_PEASHOOTER) {
                fed = FX + "GOOPEASHOOTER_PLANTFOOD/GOOPEASHOOTER_PLANTFOOD.PAM";
            }
            if (fed != null) {
                return fed;
            }
        }
        String own = fromRecord(source, SHOT_KEYS);
        if (own != null) {
            return own;
        }
        String named = OWN_SHOT.get(source);
        return named != null ? named : PEA;
    }

    public static String splatRig(Plants source, String variant) {
        if (variant != null && variant.startsWith(FED)) {
            String fed = fromRecord(source, FED_SPLAT_KEYS);
            if (fed != null) {
                return fed;
            }
        }
        String lobbed = fromRecord(source, LOB_SPLAT_KEYS);
        if (lobbed != null) {
            return lobbed;
        }
        String own = fromRecord(source, SPLAT_KEYS);
        if (own != null) {
            return own;
        }
        if (variant != null && variant.startsWith(FED)
                && fromRecord(source, FED_KEYS) != null) {
            return SPLAT_GIANT;
        }
        String named = OWN_SPLAT.get(source);
        return named != null ? named : SPLAT_PEA;
    }

    public static PamActor actor(Assets assets, Plants source, Projectile.Kind kind) {
        return actor(assets, source, kind, "");
    }

    public static PamActor actor(Assets assets, Plants source, Projectile.Kind kind,
            String variant) {
        return actor(assets, source, kind, variant, Muzzle.MAIN);
    }

    public static PamActor actor(Assets assets, Plants source, Projectile.Kind kind,
            String variant, String port) {
        if (model.entities.plants.types.FirePeashooter.FLAME.equals(port)) {
            return laneFire(assets);
        }
        return build(assets, rig(source, kind, variant), wanted(source, kind, variant));
    }

    private static final String[] BEAM_KEYS = {"FIRE", "BUBBLES", "BOLT", "AIRATTACK"};

    public static PamActor beam(Assets assets, Plants source) {
        String path = fromRecord(source, BEAM_KEYS);
        return path == null ? null : build(assets, path, "animation");
    }

    public static PamActor laneFire(Assets assets) {
        return build(assets, FX + "FIREPEASHOOTER_FIRE/FIREPEASHOOTER_FIRE.PAM", "idle");
    }

    public static PamActor splat(Assets assets, Plants source, Projectile.Kind kind,
            String variant) {
        String path = splatRig(source, variant);
        return build(assets, path, kind == Projectile.Kind.GOO ? "hit_t1" : "animation");
    }

    private static PamActor build(Assets assets, String path, String wanted) {
        if (assets == null || !assets.load(path)) {
            return null;
        }
        PamActor actor = new PamActor(assets, path, clip(assets, path, wanted)).setFit(true);
        actor.setExtent(-CANVAS / 2f, -CANVAS / 2f, CANVAS, CANVAS);
        return actor.isReady() ? actor : null;
    }

    private static String fromRecord(Plants source, String[] keys) {
        if (source == null) {
            return null;
        }
        PlantRecord record = PlantData.record(source);
        if (record == null || record.getAnimations() == null) {
            return null;
        }
        Map<String, String> effects = record.getAnimations().getEffects();
        for (String key : keys) {
            String path = effects.get(key);
            if (path != null) {
                return path;
            }
        }
        return null;
    }

    private static String clip(Assets assets, String path, String wanted) {
        List<String> clips = assets.player().clips(path);
        if (clips == null || clips.isEmpty()) {
            return wanted;
        }
        if (clips.contains(wanted)) {
            return wanted;
        }
        for (String clip : clips) {
            if (clip.toLowerCase(Locale.ROOT).startsWith("animation")) {
                return clip;
            }
        }
        return clips.get(0);
    }

    private static String wanted(Plants source, Projectile.Kind kind, String variant) {
        if (source == Plants.CABBAGE_PULT && variant != null && variant.startsWith(FED)) {
            return "animation3";
        }
        if (variant != null && variant.startsWith(FED)) {
            return kind == Projectile.Kind.ORB ? "Plantfood_Citron_Plasma_Orb" : "animation";
        }
        switch (kind) {
            case GOO: return "projectile_t1";
            case ORB: return "Citron_Citrus_Orb";
            default:  return "animation";
        }
    }
}
