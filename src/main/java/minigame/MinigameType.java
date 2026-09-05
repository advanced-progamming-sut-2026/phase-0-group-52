package minigame;

public enum MinigameType {

    VASE_BREAKER("Vasebreaker", "vasebreaker",
            "Smash every vase. Some hide a plant, some hide a zombie.",
            "BREAK THE VASES", true),
    WALLNUT_BOWLING("Wall-Nut Bowling", "wallnut_bowling",
            "Bowl wall-nuts down the lane and ricochet them through the horde.",
            "BOWL THEM OVER", true),
    I_ZOMBIE("I, Zombie", "i_zombie",
            "Play the other side. Buy zombies with sun and eat every brain.",
            "EAT THE BRAINS", true),
    BEGHOULED("Beghouled", "beghouled",
            "Swap neighbouring plants to line up three and clear the garden.",
            "MATCH THREE", true),
    ZOMBOTANY("Zombotany", "zombotany",
            "The zombies grew plant heads, and they shoot back.",
            "SURVIVE ZOMBOTANY", true),
    SCORE("Score Attack", "score",
            "No house to defend - just survive and run the score up.",
            "RUN UP THE SCORE", true),
    JOUST("Joust", "joust",
            "Two gardens, one lance. Arrives in phase 3.",
            "COMING IN PHASE 3", false);

    private final String displayName;
    private final String iconName;
    private final String blurb;
    private final String tag;
    private final boolean playable;

    MinigameType(String displayName, String iconName, String blurb, String tag,
            boolean playable) {
        this.displayName = displayName;
        this.iconName = iconName;
        this.blurb = blurb;
        this.tag = tag;
        this.playable = playable;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIconName() {
        return iconName;
    }

    public String getBlurb() {
        return blurb;
    }

    public String getTag() {
        return tag;
    }

    public boolean isPlayable() {
        return playable;
    }

    public boolean isLawnBased() {
        return this != BEGHOULED && this != JOUST;
    }

    public static MinigameType byKey(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().replace("_", "").replace("-", "").replace(" ", "");
        for (MinigameType type : values()) {
            if (type.name().replace("_", "").equalsIgnoreCase(normalized)
                    || type.iconName.replace("_", "").equalsIgnoreCase(normalized)) {
                return type;
            }
        }
        return null;
    }

    public static String keys() {
        StringBuilder out = new StringBuilder();
        for (MinigameType type : values()) {
            if (out.length() > 0) {
                out.append(", ");
            }
            out.append(type.iconName);
        }
        return out.toString();
    }
}
