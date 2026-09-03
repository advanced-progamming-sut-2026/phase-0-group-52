package model.shop;

import model.App;
import model.User;
import model.entities.plants.PlantData;
import model.entities.plants.PlantRecord;
import model.entities.plants.Plants;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShopRulesTest {

    @Test
    void plantFoodIsCarriedOnTheUserAndCapsAtFive() {
        User user = new User();
        user.addPlantFood(3);
        assertEquals(3, user.getPlantFood(), "it is stored on the player");
        user.addPlantFood(9);
        assertEquals(User.MAX_PLANT_FOOD, user.getPlantFood(), "and it stops at five");
        assertEquals(5, User.MAX_PLANT_FOOD, "five, as the shop advertises");
        assertEquals(User.MAX_PLANT_FOOD, Shop.PLANT_FOOD_MAX,
                "the shop must quote the real cap");
    }

    @Test
    void seedPacketsOnlyEverComeFromPlantsYouOwn() {
        Shop shop = new Shop();
        User user = new User();
        assertTrue(shop.ownedPlants(user).size() > 0, "a new player owns the starters");
        for (Plants owned : shop.ownedPlants(user)) {
            assertTrue(user.getPlants().isUnlocked(owned),
                    owned.getName() + " is offered but not unlocked");
        }
        Plants picked = shop.randomOwned(user);
        assertNotNull(picked, "a random packet has something to pick from");
        assertTrue(user.getPlants().isUnlocked(picked), "and it is a plant you own");
    }

    @Test
    void theDailyDealOffersALockedPremiumOrMint() {
        Shop shop = new Shop();
        User user = new User();
        Plants offer = shop.dailyPlant(user);
        assertNotNull(offer, "there should be something to unlock");
        assertTrue(!user.getPlants().isUnlocked(offer),
                offer.getName() + " is already unlocked and should not be offered");
        PlantRecord record = PlantData.record(offer);
        assertTrue(record.getUnlockKind() == PlantRecord.UnlockKind.PREMIUM
                        || record.getUnlockKind() == PlantRecord.UnlockKind.MINT,
                offer.getName() + " is not a premium or mint");
    }

    @Test
    void theDailyDealMovesOnOnceYouOwnIt() {
        Shop shop = new Shop();
        User user = new User();
        Plants first = shop.dailyPlant(user);
        user.getPlants().grant(first, 1);
        Plants second = shop.dailyPlant(user);
        assertTrue(second == null || second != first,
                "a plant you already own must not stay on offer");
    }

    @Test
    void aChosenBundleIsThreePackets() {
        assertEquals(3, Shop.CHOICE_SEEDS_COUNT, "three, not ten");
    }

    @Test
    void aLevelStartsWithThePlantFoodYouAreCarrying() {
        App app = App.getInstance();
        User user = new User();
        user.addPlantFood(4);
        app.setLoggedInUser(user);
        model.Game game = model.LevelBuilder.build(app, model.ChapterType.ANCIENT_EGYPT, 1);
        assertEquals(4, game.getPlantFoodCount(),
                "the level should begin with what the player stored");
    }
}
