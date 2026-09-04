package model;

import model.entities.plants.Plants;

import java.util.ArrayList;
import java.util.List;

public class GameStats {

    public static final class PlantPlacement {
        private final Plants type;
        private final int col;
        private final int row;

        public PlantPlacement(Plants type, int col, int row) {
            this.type = type;
            this.col = col;
            this.row = row;
        }

        public Plants getType() { return type; }
        public int getCol() { return col; }
        public int getRow() { return row; }
    }

    private int zombiesKilled;
    private int sunCollected;
    private int plantsLost;
    private int killsAtColZeroNoMower;
    private int mowerKills;
    private int firstWaveTick = -1;
    private int finalSun;

    private final List<PlantPlacement> plantsPlanted = new ArrayList<>();
    private final List<Integer> killTicks = new ArrayList<>();
    private final java.util.Set<String> killedZombies = new java.util.LinkedHashSet<>();

    public void recordKill(int tick) {
        zombiesKilled++;
        killTicks.add(tick);
    }

    public void recordKill(int tick, String alias) {
        recordKill(tick);
        if (alias != null) {
            killedZombies.add(alias);
        }
    }

    public java.util.Set<String> getKilledZombies() {
        return java.util.Collections.unmodifiableSet(killedZombies);
    }

    public void recordMowerKill() {
        mowerKills++;
    }

    public void recordKillAtColZeroNoMower() {
        killsAtColZeroNoMower++;
    }

    public void addSunCollected(int amount) {
        sunCollected += amount;
    }

    public void recordPlantPlanted(Plants type, int col, int row) {
        plantsPlanted.add(new PlantPlacement(type, col, row));
    }

    public void recordPlantLost() {
        plantsLost++;
    }

    public void recordFirstWave(int tick) {
        if (firstWaveTick < 0) {
            firstWaveTick = tick;
        }
    }

    public void setFinalSun(int finalSun) {
        this.finalSun = finalSun;
    }

    public int getZombiesKilled() { return zombiesKilled; }
    public int getSunCollected() { return sunCollected; }
    public int getPlantsLost() { return plantsLost; }
    public int getKillsAtColZeroNoMower() { return killsAtColZeroNoMower; }
    public int getMowerKills() { return mowerKills; }
    public int getFirstWaveTick() { return firstWaveTick; }
    public int getFinalSun() { return finalSun; }
    public List<PlantPlacement> getPlantsPlanted() { return plantsPlanted; }
    public List<Integer> getKillTicks() { return killTicks; }

    public int killsWithinTicksOfFirstWave(int windowTicks) {
        if (firstWaveTick < 0) {
            return 0;
        }
        int count = 0;
        for (int tick : killTicks) {
            if (tick - firstWaveTick <= windowTicks) {
                count++;
            }
        }
        return count;
    }
}
