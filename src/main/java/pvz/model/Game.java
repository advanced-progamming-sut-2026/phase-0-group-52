package pvz.model;

import pvz.model.entities.Sun;
import pvz.model.entities.plants.Plant;
import pvz.model.entities.plants.Plants;
import pvz.model.entities.zombies.Zombie;
import pvz.model.level.Level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Game {
    public static final int MAX_PLANT_FOOD = 3;

    private App app;
    private Chapter chapter;
    private Level level;
    private GameField field;

    private int sunAmount;
    private int plantFoodCount;

    private final ArrayList<Plant> plants;
    private final ArrayList<Zombie> zombies;
    private final ArrayList<Sun> suns;
    private final ArrayList<Wave> waves;

    private int currentTick;
    private int currentWaveIndex;
    private int nextSunDropTick;

    private final Map<Plants, Double> cooldowns = new HashMap<>();
    private final Set<Plants> boostedTypes = new HashSet<>();

    private final ArrayList<Plants> chosenPlants = new ArrayList<>();

    private boolean cooldownsRemoved;

    private boolean gameOver;
    private boolean won;

    public Game(App app, Chapter chapter, Level level, GameField field, int sunAmount,
                ArrayList<Plant> plants, ArrayList<Wave> waves) {
        this.app = app;
        this.chapter = chapter;
        this.level = level;
        this.field = field;
        this.sunAmount = sunAmount;
        this.plants = (plants != null) ? plants : new ArrayList<>();
        this.waves = (waves != null) ? waves : new ArrayList<>();
        this.zombies = new ArrayList<>();
        this.suns = new ArrayList<>();
        this.currentTick = 0;
        this.currentWaveIndex = -1;
        this.nextSunDropTick = 0;
        this.plantFoodCount = 0;
        this.gameOver = false;
        this.won = false;
    }

    public ArrayList<Plant> getPlantsAt(int col, int row) {
        ArrayList<Plant> result = new ArrayList<>();
        for (Plant p : plants) {
            if (p.getCol() == col && p.getRow() == row) result.add(p);
        }
        return result;
    }

    public ArrayList<Zombie> getZombiesInRow(int row) {
        ArrayList<Zombie> result = new ArrayList<>();
        for (Zombie z : zombies) {
            if (z.getRow() == row) result.add(z);
        }
        return result;
    }

    public boolean isOnCooldown(Plants type) {
        return cooldowns.getOrDefault(type, 0.0) > 0;
    }

    public double getRemainingCooldown(Plants type) {
        return cooldowns.getOrDefault(type, 0.0);
    }

    public void startCooldown(Plants type) {
        cooldowns.put(type, (double) type.getRecharge());
    }

    public void clearAllCooldowns() {
        cooldowns.clear();
    }

    public boolean isCooldownsRemoved() { return cooldownsRemoved; }
    public void setCooldownsRemoved(boolean cooldownsRemoved) { this.cooldownsRemoved = cooldownsRemoved; }

    public ArrayList<Plants> getChosenPlants() { return chosenPlants; }

    public void addSun(int amount) {
        this.sunAmount += amount;
    }

    public boolean spendSun(int amount) {
        if (sunAmount < amount) return false;
        sunAmount -= amount;
        return true;
    }

    public App getApp() { return app; }
    public void setApp(App app) { this.app = app; }

    public Chapter getChapter() { return chapter; }
    public void setChapter(Chapter chapter) { this.chapter = chapter; }

    public Level getLevel() { return level; }
    public void setLevel(Level level) { this.level = level; }

    public GameField getField() { return field; }
    public void setField(GameField field) { this.field = field; }

    public int getSunAmount() { return sunAmount; }
    public void setSunAmount(int sunAmount) { this.sunAmount = sunAmount; }

    public int getPlantFoodCount() { return plantFoodCount; }
    public void setPlantFoodCount(int plantFoodCount) {
        this.plantFoodCount = Math.min(plantFoodCount, MAX_PLANT_FOOD);
    }

    public ArrayList<Plant> getPlants() { return plants; }
    public ArrayList<Zombie> getZombies() { return zombies; }
    public ArrayList<Sun> getSuns() { return suns; }
    public ArrayList<Wave> getWaves() { return waves; }

    public int getCurrentTick() { return currentTick; }
    public void setCurrentTick(int currentTick) { this.currentTick = currentTick; }

    public int getCurrentWaveIndex() { return currentWaveIndex; }
    public void setCurrentWaveIndex(int currentWaveIndex) { this.currentWaveIndex = currentWaveIndex; }

    public int getNextSunDropTick() { return nextSunDropTick; }
    public void setNextSunDropTick(int nextSunDropTick) { this.nextSunDropTick = nextSunDropTick; }

    public Map<Plants, Double> getCooldowns() { return cooldowns; }
    public Set<Plants> getBoostedTypes() { return boostedTypes; }

    public boolean isGameOver() { return gameOver; }
    public void setGameOver(boolean gameOver) { this.gameOver = gameOver; }

    public boolean isWon() { return won; }
    public void setWon(boolean won) { this.won = won; }
}
