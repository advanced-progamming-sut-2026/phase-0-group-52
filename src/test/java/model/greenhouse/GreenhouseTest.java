package model.greenhouse;

import model.App;
import model.User;
import model.entities.plants.Plants;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GreenhouseTest {

    @Test
    void aGardenHasTwelveSlotsAndStartsWithOnePot() {
        Greenhouse garden = new Greenhouse();
        assertEquals(12, Greenhouse.SLOTS, "the garden is three rows of four");
        assertEquals(12, garden.slots().size(), "every slot should exist");
        assertEquals(1, garden.unlockedPotCount(), "you begin with a single pot");
    }

    @Test
    void buyingPotsFillsTheGridOneSlotAtATime() {
        Greenhouse garden = new Greenhouse();
        for (int bought = 1; bought < Greenhouse.SLOTS; bought++) {
            assertTrue(garden.unlockNextPot(), "pot " + bought + " should fit");
        }
        assertEquals(Greenhouse.SLOTS, garden.unlockedPotCount(), "the garden fills up");
        assertFalse(garden.unlockNextPot(), "and then it is full");
    }

    @Test
    void aFreshSproutIsSmallAndGrowsOverHalfAnHour() {
        Pot pot = new Pot(1, 1, true);
        pot.plantSpecial(Plants.SUNFLOWER);
        assertEquals(Pot.SEEDLING_SCALE, pot.visualScale(), 0.01f, "it starts small");
        assertFalse(pot.isReady(), "it is not ready the moment it is planted");

        long now = System.currentTimeMillis();
        pot.setTimestamps(now - Pot.GROW_MILLIS / 2L, now + Pot.GROW_MILLIS / 2L);
        assertTrue(pot.visualScale() > Pot.SEEDLING_SCALE, "halfway it is visibly bigger");
        assertFalse(pot.isReady(), "but still growing");

        pot.setTimestamps(now - Pot.GROW_MILLIS, now - 1L);
        assertTrue(pot.isReady(), "past its ready time it is grown");
        assertEquals(1f, pot.visualScale(), 0.01f, "and full size");
        assertTrue(pot.caption().contains("fully grown"), "and it says so");
    }

    @Test
    void aPotHoldsWaterOnlyForAnAquaticPlant() {
        Pot dry = new Pot(1, 1, true);
        dry.plantSpecial(Plants.SUNFLOWER);
        assertFalse(dry.holdsWaterPlant(), "a sunflower sits in soil");

        Pot wet = new Pot(1, 1, true);
        wet.plantSpecial(Plants.LILY_PAD);
        assertTrue(wet.holdsWaterPlant(), "a lily pad needs water");
    }

    @Test
    void wateringPaysOutAndTheMarigoldStartsOver() {
        App app = App.getInstance();
        User user = new User();
        user.setGems(10);
        user.setCoins(0);
        app.setLoggedInUser(user);
        Pot pot = app.getGreenhouse().getPot(1, 1);
        pot.setUnlocked(true);
        pot.clear();
        pot.plantMarigold();

        controller.menu.GreenhouseController tender =
                new controller.menu.GreenhouseController(app);
        assertTrue(tender.pickUpCan(), "picking the can up is free");
        assertEquals(10, user.getGems(), "nothing is charged until it is used");
        assertTrue(tender.water(pot), "watering an unripe pot works");
        assertEquals(10 - controller.menu.GreenhouseController.CAN_PRICE, user.getGems(),
                "the gems are spent on use");
        assertFalse(tender.holdingCan(), "the can is put down after use");

        pot.finishGrowth();
        int coins = tender.harvest(pot);
        assertTrue(coins >= controller.menu.GreenhouseController.MARIGOLD_MIN
                        && coins <= controller.menu.GreenhouseController.MARIGOLD_MAX,
                "a marigold pays between thirty and fifty coins, saw " + coins);
        assertTrue(pot.isOccupied(),
                "a harvested marigold stays in its pot and grows again");
        assertFalse(pot.isReady(), "and it starts over from the beginning");
        assertEquals(0, coins % controller.menu.GreenhouseController.COIN_BATCH,
                "coins come in batches of ten, saw " + coins);
    }

    @Test
    void sowingSpendsASproutAndOnlyGrowsBoostablePlants() {
        App app = App.getInstance();
        User user = new User();
        user.setSprouts(1);
        app.setLoggedInUser(user);
        Pot pot = app.getGreenhouse().getPot(2, 1);
        pot.setUnlocked(true);
        pot.clear();

        controller.menu.GreenhouseController tender =
                new controller.menu.GreenhouseController(app);
        assertTrue(tender.sow(pot), "one sprout should plant");
        assertEquals(0, user.getSprouts(), "and it is spent");
        assertTrue(pot.isOccupied(), "the pot now holds something");
        if (pot.getPlantType() != null) {
            assertTrue(model.entities.plants.PlantData.record(pot.getPlantType())
                            .isBoostable(),
                    "sprouts only ever grow boostable plants");
        }
        assertFalse(tender.sow(pot), "with no sprouts left, nothing more grows");
    }

    @Test
    void theSamePlantNeverGrowsInTwoPotsButMarigoldsMay() {
        App app = App.getInstance();
        User user = new User();
        app.setLoggedInUser(user);
        Greenhouse garden = user.getGreenhouse();
        for (int i = 1; i < Greenhouse.SLOTS; i++) {
            garden.unlockNextPot();
        }
        controller.menu.GreenhouseController tender =
                new controller.menu.GreenhouseController(app);
        user.setSprouts(Greenhouse.SLOTS);
        for (Pot pot : garden.slots()) {
            tender.sow(pot);
        }
        java.util.Set<Plants> seen = new java.util.HashSet<Plants>();
        int marigolds = 0;
        for (Pot pot : garden.slots()) {
            if (!pot.isOccupied()) {
                continue;
            }
            if (pot.isMarigold()) {
                marigolds++;
                continue;
            }
            assertTrue(seen.add(pot.getPlantType()),
                    pot.getPlantType().getName() + " is growing in two pots at once");
        }
        assertTrue(marigolds >= 0, "marigolds may repeat freely");
    }

    @Test
    void aGardenIsPerUser() {
        User one = new User();
        User two = new User();
        one.getGreenhouse().unlockNextPot();
        assertEquals(2, one.getGreenhouse().unlockedPotCount(),
                "the first user bought a pot");
        assertEquals(1, two.getGreenhouse().unlockedPotCount(),
                "the second user's garden is untouched");
    }
}
