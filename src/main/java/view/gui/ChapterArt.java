package view.gui;

import model.ChapterType;
import model.entities.plants.PlantRecord;

public final class ChapterArt {

    private ChapterArt() {}

    public static String world(ChapterType chapter) {
        if (chapter == null) {
            return "EGYPT";
        }
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

    public static String turf(PlantRecord record) {
        if (record == null) {
            return "IMAGE_BACKGROUNDS_FRONTLAWN_TEXTURE";
        }
        if (record.getChapter() != null) {
            return "IMAGE_BACKGROUNDS_" + world(record.getChapter()) + "_TEXTURE";
        }
        if (record.getUnlockKind() == PlantRecord.UnlockKind.PREMIUM) {
            return "IMAGE_BACKGROUNDS_RIFT_TEXTURE";
        }
        if (record.getUnlockKind() == PlantRecord.UnlockKind.MINT) {
            return "IMAGE_BACKGROUNDS_DINO_TEXTURE";
        }
        return "IMAGE_BACKGROUNDS_FRONTLAWN_TEXTURE";
    }
}
