package model.entities.plants;

import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CategoryMergeTest {

    @Test
    void everyCategoryHasExactlyOneMint() {
        Map<PlantsCategory, Integer> mints = new EnumMap<PlantsCategory, Integer>(
                PlantsCategory.class);
        for (Plants plant : Plants.values()) {
            if (plant.name().endsWith("_MINT")) {
                PlantsCategory category = PlantData.record(plant).getCategory();
                Integer seen = mints.get(category);
                mints.put(category, seen == null ? 1 : seen + 1);
            }
        }
        assertEquals(8, mints.size());
        for (Map.Entry<PlantsCategory, Integer> entry : mints.entrySet()) {
            assertEquals(1, entry.getValue().intValue(), entry.getKey().name());
        }
    }

    @Test
    void noPlantUsesTheRetiredCategories() {
        for (PlantsCategory category : PlantsCategory.values()) {
            assertFalse("HOMING".equals(category.name()));
            assertFalse("MODIFIER".equals(category.name()));
        }
        boolean magic = false;
        for (Plants plant : Plants.values()) {
            magic |= PlantData.record(plant).getCategory() == PlantsCategory.MAGIC;
        }
        assertTrue(magic);
    }

    @Test
    void theReplacementPlantsHaveFullArt() {
        for (Plants plant : new Plants[]{Plants.SNAPDRAGON, Plants.LIGHTNING_REED}) {
            PlantRecord record = PlantData.record(plant);
            assertNotNull(record.getCodeName(), plant.getName());
            assertNotNull(record.getPacketIcon(), plant.getName());
            assertTrue(record.getIconWidth() > 0, plant.getName());
            assertTrue(record.getAnimations().hasPlant(), plant.getName());
            assertEquals(PlantsCategory.STRIKE_THROUGH, record.getCategory());
        }
    }

    @Test
    void everyPlantStillHasAnIconAndAnimation() {
        for (Plants plant : Plants.values()) {
            PlantRecord record = PlantData.record(plant);
            assertNotNull(record.getPacketIcon(), plant.getName());
            assertTrue(record.getAnimations().hasPlant(), plant.getName());
        }
    }
}
