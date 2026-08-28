package model.adventure;

import model.ChapterType;
import model.entities.plants.PlantCollection;
import model.entities.plants.PlantData;
import model.entities.plants.Plants;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlantIslandTest {

    @Test
    void tenIslandsHandOutTheChaptersTenPlantsExactlyOnceEach() {
        for (ChapterType chapter : ChapterType.values()) {
            AdventureProgress progress = new AdventureProgress();
            PlantCollection owned = new PlantCollection();
            Random random = new Random(7);
            Set<Plants> granted = new HashSet<Plants>();

            for (int slot = 0; slot < ChapterMap.PLANT_ISLANDS; slot++) {
                Plants prize = progress.nextPrize(owned, chapter, random);
                assertNotNull(prize, chapter + " ran dry at slot " + slot);
                assertTrue(granted.add(prize), chapter + " handed out " + prize + " twice");
                progress.record(chapter, slot, prize);
                owned.grant(prize, 1);
            }

            assertEquals(new HashSet<Plants>(PlantData.ofChapter(chapter)),
                    granted, chapter.name());
            assertEquals(ChapterMap.PLANT_ISLANDS, progress.claimedCount(chapter));
            assertTrue(progress.remaining(chapter).isEmpty());
            assertNull(progress.nextPrize(owned, chapter, random));
        }
    }

    @Test
    void theOrderVariesBetweenPlayers() {
        Set<String> sequences = new HashSet<String>();
        for (int seed = 0; seed < 12; seed++) {
            sequences.add(sequenceFor(new Random(seed)));
        }
        assertTrue(sequences.size() > 1,
                "expected the island order to be shuffled per player");
    }

    @Test
    void aPlantAlreadyOwnedIsNotSpentOnAnIslandWhileFreshOnesRemain() {
        ChapterType chapter = ChapterType.ANCIENT_EGYPT;
        List<Plants> chapterPlants = PlantData.ofChapter(chapter);
        PlantCollection owned = new PlantCollection();
        Plants bought = chapterPlants.get(0);
        owned.grant(bought, 1);

        AdventureProgress progress = new AdventureProgress();
        Random random = new Random(3);
        for (int slot = 0; slot < chapterPlants.size() - 1; slot++) {
            Plants prize = progress.nextPrize(owned, chapter, random);
            assertTrue(prize != bought, "the shop plant was spent early");
            progress.record(chapter, slot, prize);
            owned.grant(prize, 1);
        }
        assertEquals(bought, progress.nextPrize(owned, chapter, random));
    }

    @Test
    void slotsRecordWhichPlantCameOutOfWhichIsland() {
        ChapterType chapter = ChapterType.DARK_AGES;
        AdventureProgress progress = new AdventureProgress();
        Plants prize = PlantData.ofChapter(chapter).get(0);

        assertNull(progress.claimedPlant(chapter, 4));
        progress.record(chapter, 4, prize);
        assertEquals(prize, progress.claimedPlant(chapter, 4));
        assertTrue(progress.isClaimed(chapter, 4));
        assertEquals(1, progress.claimedCount(chapter));
        assertEquals(0, progress.claimedCount(ChapterType.BIG_WAVE_BEACH));

        progress.record(chapter, -1, prize);
        progress.record(chapter, ChapterMap.PLANT_ISLANDS, prize);
        assertEquals(1, progress.claimedCount(chapter));
    }

    private String sequenceFor(Random random) {
        ChapterType chapter = ChapterType.ANCIENT_EGYPT;
        AdventureProgress progress = new AdventureProgress();
        PlantCollection owned = new PlantCollection();
        List<String> order = new ArrayList<String>();
        for (int slot = 0; slot < ChapterMap.PLANT_ISLANDS; slot++) {
            Plants prize = progress.nextPrize(owned, chapter, random);
            order.add(prize.name());
            progress.record(chapter, slot, prize);
            owned.grant(prize, 1);
        }
        return order.toString();
    }
}
