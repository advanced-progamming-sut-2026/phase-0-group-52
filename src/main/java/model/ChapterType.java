package model;

public enum ChapterType {
    ANCIENT_EGYPT("Ancient Egypt"),
    FROSTBITE_CAVES("Frostbite Caves"),
    DARK_AGES("Dark Ages"),
    BIG_WAVE_BEACH("Big Wave Beach");

    public static final int LEVELS_PER_CHAPTER = 4;

    private final String displayName;

    ChapterType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int number() {
        return ordinal() + 1;
    }

    public static ChapterType first() {
        return values()[0];
    }

    public static ChapterType byNumber(int number) {
        ChapterType[] all = values();
        if (number < 1 || number > all.length) {
            return null;
        }
        return all[number - 1];
    }

    public static String options() {
        StringBuilder out = new StringBuilder();
        for (ChapterType chapter : values()) {
            if (out.length() > 0) {
                out.append(", ");
            }
            out.append(chapter.name());
        }
        return out.toString();
    }
}
