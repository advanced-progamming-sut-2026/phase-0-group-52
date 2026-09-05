package model.level;

import minigame.MinigameType;
import model.ChapterType;
import model.Game;
import model.Vec2;
import model.entities.Cell;
import model.entities.plants.Plant;
import model.entities.plants.PlantCombat;
import model.entities.plants.PlantFactory;
import model.entities.plants.Plants;
import model.entities.zombies.Zombie;
import model.entities.zombies.ZombieFactory;
import model.entities.zombies.Zombies;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class IZombieLevel extends MinigameLevel {

    public static final int START_BRAINS = 350;
    public static final int GARDEN_COLUMNS = 4;
    public static final int DROP_COLUMN = 8;
    public static final double EAT_LINE = 0d;

    private static final Plants[] GARDEN = {
        Plants.PEASHOOTER, Plants.SUNFLOWER, Plants.WALL_NUT, Plants.SNOW_PEA,
    };

    public static final double RECHARGE_SECONDS = 5d;

    private final Map<Zombies, Integer> shop = new LinkedHashMap<Zombies, Integer>();
    private final Map<Zombies, Double> cooling = new LinkedHashMap<Zombies, Double>();
    private final List<Plant> brains = new ArrayList<Plant>();

    private boolean laidOut;

    public IZombieLevel(int levelnumber, ChapterType chaptertype,
            ArrayList<Plants> allowedplants) {
        super(MinigameType.I_ZOMBIE, levelnumber, chaptertype, allowedplants);
        shop.put(Zombies.ZOMBIE_DEFAULT, 50);
        shop.put(Zombies.ZOMBIE_ARMOR1, 75);
        shop.put(Zombies.ZOMBIE_ARMOR2, 125);
        shop.put(Zombies.ZOMBIE_IMP, 100);
        shop.put(Zombies.ZOMBIE_GARGANTUAR, 250);
    }

    public Map<Zombies, Integer> getShop() {
        return shop;
    }

    public List<Plant> getBrains() {
        return brains;
    }

    public double rechargeLeft(Zombies type) {
        Double left = cooling.get(type);
        return left == null ? 0d : left.doubleValue();
    }

    public boolean isRecharging(Zombies type) {
        return rechargeLeft(type) > 0d;
    }

    public int priceOf(Zombies type) {
        Integer price = shop.get(type);
        return price == null ? 0 : price.intValue();
    }

    @Override
    public boolean usesChapterMechanics() {
        return false;
    }

    @Override
    public boolean isSkySunEnabled() {
        return false;
    }

    @Override
    public boolean areWavesHeld() {
        return true;
    }

    public boolean buy(Game game, Zombies type, int row) {
        int price = priceOf(type);
        if (price <= 0 || game.getSunAmount() < price || isRecharging(type)
                || row < 0 || row >= game.getField().getRows()) {
            return false;
        }
        game.setSunAmount(game.getSunAmount() - price);
        cooling.put(type, Double.valueOf(RECHARGE_SECONDS));
        Zombie bought = ZombieFactory.create(type, row, DROP_COLUMN, getChaptertype());
        game.getZombies().add(bought);
        return true;
    }

    @Override
    public void onTick(Game game) {
        if (!laidOut) {
            laidOut = true;
            layOutGarden(game);
        }
        brains.retainAll(game.getPlants());
        for (Map.Entry<Zombies, Double> entry : cooling.entrySet()) {
            if (entry.getValue().doubleValue() > 0d) {
                entry.setValue(Double.valueOf(Math.max(0d,
                        entry.getValue().doubleValue() - Game.SECONDS_PER_TICK)));
            }
        }
    }

    private void layOutGarden(Game game) {
        if (game.getField() == null) {
            return;
        }
        List<Plants> pool = new ArrayList<Plants>(Arrays.asList(GARDEN));
        int rows = game.getField().getRows();
        for (int row = 0; row < rows; row++) {
            int wanted = 2 + PlantCombat.RANDOM.nextInt(2);
            for (int made = 0; made < wanted; made++) {
                int column = PlantCombat.RANDOM.nextInt(GARDEN_COLUMNS);
                Cell cell = game.getField().getCell(column, row);
                if (cell == null || !cell.isEmpty()) {
                    continue;
                }
                Plants type = pool.get(PlantCombat.RANDOM.nextInt(pool.size()));
                Plant grown = PlantFactory.create(type, new Vec2(column, row));
                cell.getPlants().add(grown);
                game.getPlants().add(grown);
                grown.onPlanted(game);
                brains.add(grown);
            }
        }
    }

    @Override
    public String checkVictory(Game game) {
        if (!laidOut || brains.isEmpty()) {
            return laidOut ? "Every brain eaten. You win!" : null;
        }
        return null;
    }

    @Override
    public String checkDefeat(Game game) {
        if (!laidOut) {
            return null;
        }
        if (!game.getZombies().isEmpty()) {
            return null;
        }
        int cheapest = Integer.MAX_VALUE;
        for (Integer price : shop.values()) {
            cheapest = Math.min(cheapest, price.intValue());
        }
        return game.getSunAmount() < cheapest
                ? "Out of zombies and out of sun. You lose!" : null;
    }
}
