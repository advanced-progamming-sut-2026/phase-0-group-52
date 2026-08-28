package model.adventure;

import model.ChapterType;
import model.level.SpecialLevel;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChapterMapTest {

    @Test
    void theStripFollowsTheSpecOrder() {
        List<MapNode> nodes = ChapterMap.of(ChapterType.ANCIENT_EGYPT);
        assertEquals(15, nodes.size());

        MapNodeKind[] expected = {
            MapNodeKind.LEVEL,
            MapNodeKind.PLANT, MapNodeKind.PLANT, MapNodeKind.PLANT,
            MapNodeKind.SPECIAL,
            MapNodeKind.PLANT, MapNodeKind.PLANT, MapNodeKind.PLANT,
            MapNodeKind.SPECIAL,
            MapNodeKind.PLANT, MapNodeKind.PLANT, MapNodeKind.PLANT, MapNodeKind.PLANT,
            MapNodeKind.ZOMBOSS,
            MapNodeKind.TROPHY,
        };
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], nodes.get(i).getKind(), "node " + i);
            assertEquals(i, nodes.get(i).getIndex(), "index " + i);
        }
    }

    @Test
    void everyChapterHasFourPlayableLevelsNumberedOneToFour() {
        for (ChapterType chapter : ChapterType.values()) {
            int playable = 0;
            for (MapNode node : ChapterMap.of(chapter)) {
                if (node.getKind().isPlayable()) {
                    playable++;
                    assertEquals(playable, node.getLevelNumber(), chapter + " level order");
                }
            }
            assertEquals(ChapterType.LEVELS_PER_CHAPTER, playable, chapter.name());
        }
    }

    @Test
    void everyChapterHasTenDistinctlyNumberedPlantIslands() {
        for (ChapterType chapter : ChapterType.values()) {
            Set<Integer> slots = new HashSet<Integer>();
            for (MapNode node : ChapterMap.of(chapter)) {
                if (node.getKind() == MapNodeKind.PLANT) {
                    assertTrue(slots.add(node.getSlot()), chapter + " duplicate slot");
                }
            }
            assertEquals(ChapterMap.PLANT_ISLANDS, slots.size(), chapter.name());
            for (int slot = 0; slot < ChapterMap.PLANT_ISLANDS; slot++) {
                assertTrue(slots.contains(slot), chapter + " missing slot " + slot);
            }
        }
    }

    @Test
    void everySpecialTypeIsUsedByExactlyOneChapter() {
        Set<SpecialLevel> used = EnumSet.noneOf(SpecialLevel.class);
        for (ChapterType chapter : ChapterType.values()) {
            for (MapNode node : ChapterMap.of(chapter)) {
                if (node.getKind() == MapNodeKind.SPECIAL) {
                    assertNotNull(node.getSpecial(), chapter + " unnamed special");
                    assertTrue(used.add(node.getSpecial()),
                            chapter + " reused " + node.getSpecial());
                    assertFalse(node.getLabel().isEmpty(), chapter + " unlabelled special");
                }
            }
        }
        assertEquals(SpecialLevel.values().length, used.size());
    }

    @Test
    void everySpecialKeyStillResolvesThroughLevelBuilder() {
        for (SpecialLevel type : SpecialLevel.values()) {
            assertEquals(type, SpecialLevel.byKey(type.getKey()));
            assertEquals(type, SpecialLevel.byKey(type.name()));
            assertTrue(model.LevelBuilder.specialTypes().contains(type.getKey()),
                    type + " missing from the builder's list");
        }
    }

    @Test
    void slotsOpenOnTheThreeThreeFourSchedule() {
        assertEquals(0, ChapterMap.slotsUnlockedBy(0));
        assertEquals(3, ChapterMap.slotsUnlockedBy(1));
        assertEquals(6, ChapterMap.slotsUnlockedBy(2));
        assertEquals(10, ChapterMap.slotsUnlockedBy(3));
        assertEquals(10, ChapterMap.slotsUnlockedBy(4));

        for (int slot = 0; slot < 3; slot++) {
            assertEquals(1, ChapterMap.levelRequiredFor(slot));
        }
        for (int slot = 3; slot < 6; slot++) {
            assertEquals(2, ChapterMap.levelRequiredFor(slot));
        }
        for (int slot = 6; slot < 10; slot++) {
            assertEquals(3, ChapterMap.levelRequiredFor(slot));
        }

        assertFalse(ChapterMap.isSlotOpen(0, 0));
        assertTrue(ChapterMap.isSlotOpen(1, 2));
        assertFalse(ChapterMap.isSlotOpen(1, 3));
        assertTrue(ChapterMap.isSlotOpen(3, 9));
        assertFalse(ChapterMap.isSlotOpen(4, ChapterMap.PLANT_ISLANDS));
    }

    @Test
    void clearedLevelsReadsTheOneBasedChapterIndex() {
        ChapterType egypt = ChapterType.ANCIENT_EGYPT;
        ChapterType frost = ChapterType.FROSTBITE_CAVES;

        assertEquals(0, ChapterMap.clearedLevels(1, 1, egypt));
        assertEquals(2, ChapterMap.clearedLevels(1, 3, egypt));
        assertEquals(0, ChapterMap.clearedLevels(1, 3, frost));
        assertEquals(ChapterType.LEVELS_PER_CHAPTER, ChapterMap.clearedLevels(2, 1, egypt));

        assertTrue(ChapterMap.isLevelPlayable(0, 1));
        assertFalse(ChapterMap.isLevelPlayable(0, 2));
        assertTrue(ChapterMap.isLevelPlayable(3, 4));
        assertFalse(ChapterMap.isLevelPlayable(4, 5));
    }
}
