package view.gui;

import model.ChapterType;

public final class ChapterArt {

    private ChapterArt() {}

    public static String world(ChapterType chapter) {
        switch (chapter) {
            case ANCIENT_EGYPT:   return "EGYPT";
            case FROSTBITE_CAVES: return "ICEAGE";
            case DARK_AGES:       return "DARK";
            case BIG_WAVE_BEACH:  return "BEACH";
            default:              return "EGYPT";
        }
    }

    public static String island(ChapterType chapter) {
        return "IMAGE_UI_UNIVERSE_WORLDS_" + world(chapter);
    }

    public static String packet(ChapterType chapter) {
        return "IMAGE_UI_PACKETS_" + world(chapter);
    }
}
