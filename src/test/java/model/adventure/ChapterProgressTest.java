package model.adventure;

import model.ChapterType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChapterProgressTest {

    @Test
    void openingOneChapterLeavesTheOthersAlone() {
        AdventureProgress progress = new AdventureProgress();
        progress.recordCleared(ChapterType.ANCIENT_EGYPT, 2);
        progress.openChapter(ChapterType.DARK_AGES);

        assertEquals(2, progress.clearedLevels(ChapterType.ANCIENT_EGYPT));
        assertEquals(0, progress.clearedLevels(ChapterType.FROSTBITE_CAVES),
                "forcing a later chapter open must not clear the ones before it");
        assertEquals(0, progress.clearedLevels(ChapterType.DARK_AGES));
        assertTrue(progress.isChapterOpen(ChapterType.DARK_AGES));
        assertFalse(progress.isChapterOpen(ChapterType.FROSTBITE_CAVES));
    }

    @Test
    void clearingAChapterOpensOnlyTheNextOne() {
        AdventureProgress progress = new AdventureProgress();
        assertTrue(progress.isChapterOpen(ChapterType.ANCIENT_EGYPT));
        assertFalse(progress.isChapterOpen(ChapterType.FROSTBITE_CAVES));

        progress.recordCleared(ChapterType.ANCIENT_EGYPT, ChapterType.LEVELS_PER_CHAPTER);
        assertTrue(progress.isChapterOpen(ChapterType.FROSTBITE_CAVES));
        assertFalse(progress.isChapterOpen(ChapterType.DARK_AGES));
    }

    @Test
    void clearedLevelsNeverGoBackwardsAndStayCapped() {
        AdventureProgress progress = new AdventureProgress();
        progress.recordCleared(ChapterType.DARK_AGES, 3);
        progress.recordCleared(ChapterType.DARK_AGES, 1);
        assertEquals(3, progress.clearedLevels(ChapterType.DARK_AGES));

        progress.recordCleared(ChapterType.DARK_AGES, 99);
        assertEquals(ChapterType.LEVELS_PER_CHAPTER,
                progress.clearedLevels(ChapterType.DARK_AGES));
    }

    @Test
    void anOldSaveMigratesFromTheLinearPointer() {
        AdventureProgress progress = new AdventureProgress();
        assertFalse(progress.hasChapterState());
        progress.seedFrom(2, 3);

        assertEquals(ChapterType.LEVELS_PER_CHAPTER,
                progress.clearedLevels(ChapterType.ANCIENT_EGYPT));
        assertEquals(2, progress.clearedLevels(ChapterType.FROSTBITE_CAVES));
        assertEquals(0, progress.clearedLevels(ChapterType.DARK_AGES));
        assertTrue(progress.isChapterOpen(ChapterType.FROSTBITE_CAVES));
        assertFalse(progress.isChapterOpen(ChapterType.DARK_AGES));
    }
}
