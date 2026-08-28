package model.entities.plants;

import model.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlantCollectionTest {

    private User user() {
        User u = new User();
        u.setCoins(0);
        u.setGems(0);
        return u;
    }

    @Test
    void startersBeginUnlockedAndNothingElseDoes() {
        PlantCollection c = new PlantCollection();
        assertTrue(c.isUnlocked(Plants.PEASHOOTER));
        assertTrue(c.isUnlocked(Plants.SUNFLOWER));
        assertTrue(c.isUnlocked(Plants.WALL_NUT));
        assertTrue(c.isUnlocked(Plants.POTATO_MINE));
        assertTrue(c.isUnlocked(Plants.CABBAGE_PULT));
        assertEquals(5, c.unlockedCount());
        assertFalse(c.isUnlocked(Plants.THREEPEATER));
    }

    @Test
    void theFirstPacketUnlocksAPlantAndIsConsumedByTheUnlock() {
        PlantCollection c = new PlantCollection();
        assertTrue(c.grant(Plants.THREEPEATER, 1));
        assertTrue(c.isUnlocked(Plants.THREEPEATER));
        assertEquals(0, c.progress(Plants.THREEPEATER).getPackets());

        assertFalse(c.grant(Plants.THREEPEATER, 1));
        assertEquals(1, c.progress(Plants.THREEPEATER).getPackets());
    }

    @Test
    void aBulkGrantKeepsEverythingAfterTheUnlockingPacket() {
        PlantCollection c = new PlantCollection();
        assertTrue(c.grant(Plants.CITRON, 5));
        assertEquals(4, c.progress(Plants.CITRON).getPackets());
    }

    @Test
    void upgradeNeedsFullXpThenSpendsPacketsBeforeCoins() {
        User u = user();
        PlantProgress state = u.getPlants().progress(Plants.PEASHOOTER);
        assertTrue(PlantData.upgrade(u, Plants.PEASHOOTER).startsWith("Error:"));

        state.setXp(state.xpNeeded());
        assertFalse(PlantData.canUpgrade(u, Plants.PEASHOOTER));

        u.getPlants().grant(Plants.PEASHOOTER, 1);
        assertTrue(PlantData.canUpgrade(u, Plants.PEASHOOTER));
        assertFalse(PlantData.upgrade(u, Plants.PEASHOOTER).startsWith("Error:"));
        assertEquals(2, state.getLevel());
        assertEquals(0, state.getXp());
        assertEquals(0, state.getPackets());
    }

    @Test
    void coinsSubstituteForPackets() {
        User u = user();
        u.setCoins(300);
        PlantProgress state = u.getPlants().progress(Plants.PEASHOOTER);
        state.setXp(state.xpNeeded());
        assertFalse(PlantData.upgrade(u, Plants.PEASHOOTER).startsWith("Error:"));
        assertEquals(2, state.getLevel());
        assertEquals(0, u.getCoins());
    }

    @Test
    void xpStopsAtMaxLevel() {
        User u = user();
        PlantProgress state = u.getPlants().progress(Plants.PEASHOOTER);
        state.setLevel(4);
        u.getPlants().addXp(Plants.PEASHOOTER, 50);
        assertEquals(0, state.getXp());
        assertTrue(state.isMaxLevel());
        assertEquals(0, state.xpNeeded());
    }

    @Test
    void boostCostsGemsAndOnlyWorksOnPlantsWithPlantFood() {
        User u = user();
        u.setGems(10);
        u.getPlants().grant(Plants.PEASHOOTER, 1);
        assertTrue(PlantData.canBoost(u, Plants.PEASHOOTER));
        assertFalse(PlantData.boost(u, Plants.PEASHOOTER).startsWith("Error:"));
        assertTrue(u.getStoredBoosts().contains(Plants.PEASHOOTER));
        assertEquals(8, u.getGems());
        assertFalse(PlantData.canBoost(u, Plants.PEASHOOTER));

        u.getPlants().grant(Plants.CHERRY_BOMB, 1);
        assertFalse(PlantData.canBoost(u, Plants.CHERRY_BOMB));
        assertTrue(PlantData.boost(u, Plants.CHERRY_BOMB).startsWith("Error:"));
    }

    @Test
    void everyPlantHasLevelingAndBadgeData() {
        for (Plants plant : Plants.values()) {
            PlantRecord r = PlantData.record(plant);
            assertEquals(4, r.getLeveling().getMaxLevel(), plant.getName());
            assertTrue(r.getLeveling().xpToLevel(2) >= 3, plant.getName());
            assertTrue(r.getLeveling().xpToLevel(3) > r.getLeveling().xpToLevel(2), plant.getName());
            assertTrue(r.getLeveling().xpToLevel(4) > r.getLeveling().xpToLevel(3), plant.getName());
            assertEquals(300, r.getLeveling().coinsToLevel(2), plant.getName());
            assertTrue(r.getCategoryBadge().startsWith("IMAGE_UI_PACKETS_MINTFAM_"), plant.getName());
            if (r.isBoostable()) {
                assertTrue(r.getGemCost() >= 2 && r.getGemCost() <= 4, plant.getName());
            }
        }
    }
}
