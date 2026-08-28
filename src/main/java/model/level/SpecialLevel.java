package model.level;

import model.ChapterType;

public enum SpecialLevel {
    CONVEYOR("conveyor", "Conveyor Belt"),
    PLANT_WHAT_YOU_GET("plant_what_you_get", "Plant What You Get"),
    LOCKED_PLANTS("locked_plants", "Locked and Loaded"),
    SAVE_OUR_SEEDS("save_our_seeds", "Save Our Seeds"),
    DEADLINE("deadline", "Dead Line"),
    LOVE_YOUR_PLANTS("love_your_plants", "Love Your Plants"),
    NIGHT_OPS("night_ops", "Night Ops"),
    TIMED_WAR("timed_war", "Timed War");

    private final String key;
    private final String displayName;

    SpecialLevel(String key, String displayName) {
        this.key = key;
        this.displayName = displayName;
    }

    public String getKey() {
        return key;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static SpecialLevel byKey(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toLowerCase().replace("-", "").replace("_", "");
        for (SpecialLevel type : values()) {
            if (type.key.replace("_", "").equals(normalized)
                    || type.name().replace("_", "").equalsIgnoreCase(normalized)) {
                return type;
            }
        }
        return alias(normalized);
    }

    public static SpecialLevel[] of(ChapterType chapter) {
        switch (chapter) {
            case ANCIENT_EGYPT:
                return new SpecialLevel[]{SAVE_OUR_SEEDS, LOCKED_PLANTS};
            case FROSTBITE_CAVES:
                return new SpecialLevel[]{CONVEYOR, TIMED_WAR};
            case DARK_AGES:
                return new SpecialLevel[]{NIGHT_OPS, PLANT_WHAT_YOU_GET};
            case BIG_WAVE_BEACH:
                return new SpecialLevel[]{DEADLINE, LOVE_YOUR_PLANTS};
            default:
                return new SpecialLevel[]{SAVE_OUR_SEEDS, LOCKED_PLANTS};
        }
    }

    public static String keys() {
        StringBuilder out = new StringBuilder();
        for (SpecialLevel type : values()) {
            if (out.length() > 0) {
                out.append(", ");
            }
            out.append(type.key);
        }
        return out.toString();
    }

    private static SpecialLevel alias(String normalized) {
        if (normalized.equals("conveyorbelt")) {
            return CONVEYOR;
        }
        if (normalized.equals("pwyg")) {
            return PLANT_WHAT_YOU_GET;
        }
        if (normalized.equals("locked")) {
            return LOCKED_PLANTS;
        }
        if (normalized.equals("sos")) {
            return SAVE_OUR_SEEDS;
        }
        if (normalized.equals("love")) {
            return LOVE_YOUR_PLANTS;
        }
        if (normalized.equals("night")) {
            return NIGHT_OPS;
        }
        if (normalized.equals("timed")) {
            return TIMED_WAR;
        }
        return null;
    }
}
