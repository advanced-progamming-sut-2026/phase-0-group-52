package model.entities.plants;

import model.ChapterType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProgressionOrderTest {

    @Test
    void rankFollowsStarterChaptersMintsPremium() {
        assertEquals(0, rankOfKind(PlantRecord.UnlockKind.STARTER));
        assertEquals(1, rankOfChapter(ChapterType.ANCIENT_EGYPT));
        assertEquals(2, rankOfChapter(ChapterType.FROSTBITE_CAVES));
        assertEquals(3, rankOfChapter(ChapterType.DARK_AGES));
        assertEquals(4, rankOfChapter(ChapterType.BIG_WAVE_BEACH));
        assertEquals(5, rankOfKind(PlantRecord.UnlockKind.MINT));
        assertEquals(6, rankOfKind(PlantRecord.UnlockKind.PREMIUM));
    }

    @Test
    void everyPlantRanksInsideTheKnownBand() {
        for (Plants plant : Plants.values()) {
            int rank = PlantData.progressionRank(PlantData.record(plant));
            assertTrue(rank >= 0 && rank <= 6, plant.getName() + " ranked " + rank);
        }
    }

    private static int rankOfKind(PlantRecord.UnlockKind kind) {
        for (Plants plant : Plants.values()) {
            PlantRecord record = PlantData.record(plant);
            if (record.getUnlockKind() == kind) {
                return PlantData.progressionRank(record);
            }
        }
        throw new IllegalStateException("No plant with unlock kind " + kind);
    }

    private static int rankOfChapter(ChapterType chapter) {
        for (Plants plant : Plants.values()) {
            PlantRecord record = PlantData.record(plant);
            if (record.getUnlockKind() == PlantRecord.UnlockKind.CHAPTER
                    && record.getChapter() == chapter) {
                return PlantData.progressionRank(record);
            }
        }
        throw new IllegalStateException("No plant in chapter " + chapter);
    }
}
