package model.shop;

import model.User;
import model.entities.plants.PlantCombat;
import model.entities.plants.Plants;
import model.greenhouse.Greenhouse;

import java.time.LocalDate;

public class Shop {

    public static final int POT_PRICE = 2000;
    public static final int PLANT_FOOD_PRICE = 3;
    public static final int PLANT_FOOD_MAX = 3;
    public static final int RANDOM_SEEDS_PRICE = 1000;
    public static final int RANDOM_SEEDS_COUNT = 5;
    public static final int CHOICE_SEEDS_PRICE = 5;
    public static final int CHOICE_SEEDS_COUNT = 10;
    public static final int EXCHANGE_DIAMONDS = 5;
    public static final int EXCHANGE_COINS = 500;
    public static final int DAILY_BASE_PRICE = 2000;
    public static final int DAILY_PRICE = 1600;
    public static final int DAILY_COUNT = 10;

    private LocalDate offerDate;
    private Plants dailyPlant;
    private LocalDate lastDailyPurchase;

    private void refreshDaily() {
        LocalDate today = LocalDate.now();
        if (!today.equals(offerDate)) {
            offerDate = today;
            dailyPlant = randomPlant();
        }
    }

    private Plants randomPlant() {
        Plants[] all = Plants.values();
        Plants plant;
        do {
            plant = all[PlantCombat.RANDOM.nextInt(all.length)];
        } while (plant.name().endsWith("MINT"));
        return plant;
    }

    public void printList() {
        System.out.println("Permanent items:");
        System.out.println("  1. Pot | " + POT_PRICE + " coins | unlocks one greenhouse slot (max 20)");
        System.out.println("  2. Plant Food | " + PLANT_FOOD_PRICE + " diamonds | +1 plant food at level start (max "
                + PLANT_FOOD_MAX + " stored)");
        System.out.println("  3. Random Seed Packet Bundle | " + RANDOM_SEEDS_PRICE + " coins | "
                + RANDOM_SEEDS_COUNT + " seed packets for a random unlocked plant");
        System.out.println("  4. Choice Seed Packet Bundle | " + CHOICE_SEEDS_PRICE + " diamonds | "
                + CHOICE_SEEDS_COUNT + " seed packets for a plant of your choice (-t required)");
        System.out.println("  5. Currency Exchange | " + EXCHANGE_DIAMONDS + " diamonds | " + EXCHANGE_COINS+ " coins");
    }

    public void printDaily() {
        refreshDaily();
        System.out.println("Daily offer (" + offerDate + "):");
        System.out.println("  6. Special Seed Packet Bundle | " + DAILY_PRICE + " coins (20% off "
                + DAILY_BASE_PRICE + ") | " + DAILY_COUNT + " seed packets for " + dailyPlant.getName()
                + (offerDate.equals(lastDailyPurchase) ? " | already purchased today" : ""));
    }

    public String buy(User user, Greenhouse greenhouse, int itemId, int count, Plants plantType) {
        if (count <= 0) return "Error: Count must be positive.";
        switch (itemId) {
            case 1: {
                if (user.getCoins() < POT_PRICE * count) return "Error: Not enough coins.";
                int unlocked = 0;
                for (int i = 0; i < count; i++)
                    if (greenhouse.unlockNextPot()) unlocked++;
                if (unlocked == 0) return "Error: All greenhouse slots are already unlocked.";
                user.setCoins(user.getCoins() - POT_PRICE * unlocked);
                return "Unlocked " + unlocked + " greenhouse slot(s).";}
            case 2: {
                int space = PLANT_FOOD_MAX - user.getPlantFoodNum();
                if (space <= 0) return "Error: Plant food storage is full (max " + PLANT_FOOD_MAX + ").";
                int bought = Math.min(count, space);
                if (user.getGems() < PLANT_FOOD_PRICE * bought) return "Error: Not enough diamonds.";
                user.setGems(user.getGems() - PLANT_FOOD_PRICE * bought);
                user.setPlantFoodNum(user.getPlantFoodNum() + bought);
                return "Bought " + bought + " plant food(s). Stored: "
                        + user.getPlantFoodNum() + "/" + PLANT_FOOD_MAX + ".";}
            case 3: {
                if (user.getCoins() < RANDOM_SEEDS_PRICE * count) return "Error: Not enough coins.";
                user.setCoins(user.getCoins() - RANDOM_SEEDS_PRICE * count);
                Plants plant = randomPlant();
                user.getPlants().grant(plant, RANDOM_SEEDS_COUNT * count);
                return "Bought " + (RANDOM_SEEDS_COUNT * count) + " seed packets for " + plant.getName() + ".";}
            case 4: {
                if (plantType == null) return "Error: Choice bundles need -t <plant_type>.";
                if (user.getGems() < CHOICE_SEEDS_PRICE * count) return "Error: Not enough diamonds.";
                user.setGems(user.getGems() - CHOICE_SEEDS_PRICE * count);
                user.getPlants().grant(plantType, CHOICE_SEEDS_COUNT * count);
                return "Bought " + (CHOICE_SEEDS_COUNT * count) + " seed packets for " + plantType.getName() + ".";}
            case 5: {
                if (user.getGems() < EXCHANGE_DIAMONDS * count) return "Error: Not enough diamonds.";
                user.setGems(user.getGems() - EXCHANGE_DIAMONDS * count);
                user.setCoins(user.getCoins() + EXCHANGE_COINS * count);
                return "Exchanged " + (EXCHANGE_DIAMONDS * count) + " diamonds for "
                        + (EXCHANGE_COINS * count) + " coins.";}
            case 6: {
                refreshDaily();
                if (offerDate.equals(lastDailyPurchase))
                    return "Error: The daily offer can only be bought once per day.";
                if (user.getCoins() < DAILY_PRICE) return "Error: Not enough coins.";
                user.setCoins(user.getCoins() - DAILY_PRICE);
                user.getPlants().grant(dailyPlant, DAILY_COUNT);
                lastDailyPurchase = offerDate;
                return "Bought the daily bundle: " + DAILY_COUNT + " seed packets for " + dailyPlant.getName() + ".";}
            default:
                return "Error: Unknown item id: " + itemId;
        }
    }
}
