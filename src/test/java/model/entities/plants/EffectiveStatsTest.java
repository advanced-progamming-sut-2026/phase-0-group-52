package model.entities.plants;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EffectiveStatsTest {

    @Test
    void peashooterGainsDamageAndHpAndLosesCost() {
        Plants p = Plants.PEASHOOTER;
        assertEquals(p.getDamage(), PlantData.effectiveDamage(p, 1));
        assertEquals(p.getBaseHP(), PlantData.effectiveHp(p, 1));
        assertEquals(p.getCost(), PlantData.effectiveCost(p, 1));

        assertEquals(p.getDamage() + 10, PlantData.effectiveDamage(p, 2));
        assertEquals(p.getBaseHP() + 150, PlantData.effectiveHp(p, 3));
        assertEquals(p.getCost() - 25, PlantData.effectiveCost(p, 4));
    }

    @Test
    void sunflowerActionIntervalShortens() {
        Plants p = Plants.SUNFLOWER;
        double base = PlantData.effectiveInterval(p, 1);
        double upgraded = PlantData.effectiveInterval(p, 2);
        assertTrue(upgraded < base, base + " -> " + upgraded);
    }

    @Test
    void everyPlantStaysSaneAtMaxLevel() {
        for (Plants plant : Plants.values()) {
            assertTrue(PlantData.effectiveHp(plant, 4) >= 0, plant.getName());
            assertTrue(PlantData.effectiveDamage(plant, 4) >= 0, plant.getName());
            assertTrue(PlantData.effectiveCost(plant, 4) >= 0, plant.getName());
            assertTrue(PlantData.effectiveInterval(plant, 4) >= 0.1, plant.getName());
            assertTrue(PlantData.effectiveHp(plant, 4) >= PlantData.effectiveHp(plant, 1),
                    plant.getName());
        }
    }
}
