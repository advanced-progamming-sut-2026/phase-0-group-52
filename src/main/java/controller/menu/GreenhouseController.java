package controller.menu;

import controller.SaveService;
import model.App;
import model.User;
import model.entities.plants.PlantCombat;
import model.entities.plants.PlantData;
import model.entities.plants.Plants;
import model.greenhouse.Greenhouse;
import model.greenhouse.Pot;

import java.util.ArrayList;
import java.util.List;

public class GreenhouseController {

    public static final int CAN_PRICE = 2;
    public static final double MARIGOLD_ODDS = 0.3;
    public static final int MARIGOLD_MIN = 30;
    public static final int MARIGOLD_MAX = 50;
    public static final int COIN_BATCH = 10;
    public static final float WATER_SECONDS = 5f;

    private final App app;
    private final SaveService saves = new SaveService();

    public GreenhouseController(App app) {
        this.app = app;
    }

    public User user() {
        return app == null ? null : app.getLoggedInUser();
    }

    public Greenhouse garden() {
        User user = user();
        return user == null ? null : user.getGreenhouse();
    }

    public List<Pot> pots() {
        Greenhouse garden = garden();
        return garden == null ? new ArrayList<Pot>() : garden.slots();
    }

    public int potsOwned() {
        Greenhouse garden = garden();
        return garden == null ? 0 : garden.unlockedPotCount();
    }

    public boolean gardenIsFull() {
        return potsOwned() >= Greenhouse.SLOTS;
    }

    public int sprouts() {
        User user = user();
        return user == null ? 0 : user.getSprouts();
    }

    public void addSprout() {
        User user = user();
        if (user != null) {
            user.addSprouts(1);
            saves.persist(user);
        }
    }

    public boolean holdingCan() {
        User user = user();
        return user != null && user.isHoldingCan();
    }

    public boolean canAffordWatering() {
        User user = user();
        return user != null && user.getGems() >= CAN_PRICE;
    }

    public boolean pickUpCan() {
        User user = user();
        if (user == null || user.isHoldingCan()) {
            return false;
        }
        user.setHoldingCan(true);
        return true;
    }

    public void dropCan() {
        User user = user();
        if (user != null) {
            user.setHoldingCan(false);
        }
    }

    public boolean sow(Pot pot) {
        User user = user();
        if (user == null || pot == null || !pot.isUnlocked() || pot.isOccupied()
                || user.getSprouts() <= 0) {
            return false;
        }
        user.addSprouts(-1);
        Plants grown = PlantCombat.RANDOM.nextDouble() < MARIGOLD_ODDS ? null : freshPlant(user);
        if (grown == null) {
            pot.plantMarigold();
        } else {
            pot.plantSpecial(grown);
        }
        saves.persist(user);
        return true;
    }

    public boolean water(Pot pot) {
        User user = user();
        if (user == null || pot == null || !user.isHoldingCan() || !pot.isOccupied()
                || pot.isReady() || user.getGems() < CAN_PRICE) {
            return false;
        }
        user.setGems(user.getGems() - CAN_PRICE);
        user.setHoldingCan(false);
        saves.persist(user);
        return true;
    }

    public boolean dig(Pot pot) {
        if (pot == null || !pot.isOccupied()) {
            return false;
        }
        pot.clear();
        saves.persist(user());
        return true;
    }

    public int harvest(Pot pot) {
        User user = user();
        if (user == null || pot == null || !pot.isMarigold() || !pot.isReady()) {
            return 0;
        }
        int batches = MARIGOLD_MIN / COIN_BATCH
                + PlantCombat.RANDOM.nextInt((MARIGOLD_MAX - MARIGOLD_MIN) / COIN_BATCH + 1);
        pot.restart();
        saves.persist(user);
        return batches * COIN_BATCH;
    }

    public void collect(int coins) {
        User user = user();
        if (user != null && coins > 0) {
            user.setCoins(user.getCoins() + coins);
            saves.persist(user);
        }
    }

    public boolean boostSpent(Pot pot) {
        User user = user();
        return user != null && pot != null && pot.isOccupied() && !pot.isMarigold()
                && pot.isReady() && !user.getStoredBoosts().contains(pot.getPlantType());
    }

    public void regrow(Pot pot) {
        if (pot != null) {
            pot.restart();
            saves.persist(user());
        }
    }

    public void ripen(Pot pot) {
        User user = user();
        if (user == null || pot == null || pot.isMarigold() || !pot.isOccupied()) {
            return;
        }
        user.getStoredBoosts().add(pot.getPlantType());
        saves.persist(user);
    }

    private Plants freshPlant(User user) {
        List<Plants> able = new ArrayList<Plants>();
        for (Plants type : Plants.values()) {
            if (PlantData.record(type) == null || !PlantData.record(type).isBoostable()) {
                continue;
            }
            if (!user.getPlants().isUnlocked(type) || alreadyGrowing(type)) {
                continue;
            }
            able.add(type);
        }
        return able.isEmpty() ? null : able.get(PlantCombat.RANDOM.nextInt(able.size()));
    }

    private boolean alreadyGrowing(Plants type) {
        for (Pot pot : pots()) {
            if (pot.isOccupied() && pot.getPlantType() == type) {
                return true;
            }
        }
        return false;
    }
}
