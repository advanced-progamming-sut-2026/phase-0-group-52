package model.entities.plants;

import model.ChapterType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlantDataTest {

    @Test
    void everyPlantHasARecord() {
        for (Plants plant : Plants.values()) {
            assertNotNull(PlantData.record(plant), plant.getName());
        }
    }

    @Test
    void recordMatchesTheEnum() {
        for (Plants plant : Plants.values()) {
            PlantRecord record = PlantData.record(plant);
            assertEquals(plant.getCategory(), record.getCategory(), plant.getName());
            assertEquals(plant.getName(), record.getName());
        }
    }

    @Test
    void upgradesStillLoad() {
        assertEquals(3, PlantData.getUpgrades(Plants.SUNFLOWER).size());
        assertEquals(50, PlantData.effectiveCost(Plants.SUNFLOWER, 4));
        assertEquals(125, PlantData.effectiveCost(Plants.TWIN_SUNFLOWER, 3));
        assertEquals(100, PlantData.effectiveCost(Plants.TWIN_SUNFLOWER, 4));
    }

    @Test
    void chaptersAreBalanced() {
        int total = 0;
        for (ChapterType chapter : ChapterType.values()) {
            int count = PlantData.ofChapter(chapter).size();
            assertEquals(10, count, chapter.name());
            total += count;
        }
        assertEquals(40, total);
    }

    @Test
    void seedPacketArtIsPresent() {
        for (Plants plant : Plants.values()) {
            PlantRecord record = PlantData.record(plant);
            assertTrue(record.getPacketBackground().startsWith("IMAGE_UI_PACKETS_"),
                    plant.getName());
        }
    }

    @Test
    void animationPathsAreRooted() {
        for (Plants plant : Plants.values()) {
            for (String path : PlantData.record(plant).getAnimations().allPaths()) {
                assertTrue(path.startsWith("768/") && path.endsWith(".PAM"), path);
            }
        }
    }
}
