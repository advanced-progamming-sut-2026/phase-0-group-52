package controller.menu;

import controller.SaveService;
import model.App;
import model.Result;
import model.User;
import model.entities.plants.PlantData;
import model.entities.plants.Plants;
import model.entities.zombies.ZombieData;
import model.entities.zombies.ZombieRecord;

public class CollectionMenuController {

    public static final int PLANT_PURCHASE_PRICE = 1000;

    private final App app;
    private final SaveService saves = new SaveService();

    public CollectionMenuController(App app) {
        this.app = app;
    }

    public Result upgradePlant(Plants plant) {
        User user = app.getCurrentuser();
        if (user == null) {
            return failure("No user is signed in.");
        }
        if (plant == null) {
            return failure("Unknown plant.");
        }
        String outcome = PlantData.upgrade(user, plant);
        saves.persist(user);
        return new Result(true, outcome, plant);
    }

    public Result boostPlant(Plants plant) {
        User user = app.getCurrentuser();
        if (user == null) {
            return failure("No user is signed in.");
        }
        if (plant == null) {
            return failure("Unknown plant.");
        }
        String outcome = PlantData.boost(user, plant);
        saves.persist(user);
        return new Result(true, outcome, plant);
    }

    public Result buyPlant(Plants plant) {
        User user = app.getCurrentuser();
        if (user == null) {
            return failure("No user is signed in.");
        }
        if (plant == null) {
            return failure("Unknown plant.");
        }
        if (user.getCoins() < PLANT_PURCHASE_PRICE) {
            return failure(plant.getName() + " costs "
                    + PLANT_PURCHASE_PRICE + " coins and you cannot afford it.");
        }
        user.setCoins(user.getCoins() - PLANT_PURCHASE_PRICE);
        user.getPlants().grant(plant, 1);
        user.getNewsList().addNews("New plant unlocked: " + plant.getName()
                + "! Add it to your deck.");
        saves.persist(user);
        return new Result(true, "Bought " + plant.getName() + " for "
                + PLANT_PURCHASE_PRICE + " coins.", plant);
    }

    public Result unlockZombie(String alias) {
        User user = app.getCurrentuser();
        if (user == null) {
            return failure("No user is signed in.");
        }
        ZombieRecord record = ZombieData.byAlias(alias);
        if (record == null) {
            return failure("Unknown zombie: " + alias);
        }
        if (user.markZombieSeen(alias)) {
            saves.persist(user);
        }
        return new Result(true, record.getName() + " added to the almanac.", record);
    }

    public Plants findPlant(String input) {
        if (input == null) {
            return null;
        }
        String normalized = input.trim().replace(' ', '_').replace('-', '_');
        for (Plants plant : Plants.values()) {
            if (plant.getName().equalsIgnoreCase(input.trim())
                    || plant.name().equalsIgnoreCase(normalized)) {
                return plant;
            }
        }
        return null;
    }

    private Result failure(String message) {
        return new Result(false, message, null);
    }
}
