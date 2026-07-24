package model;

import model.entities.plants.Plants;

import java.util.ArrayList;
import java.util.List;

public class GameStats {

    private final List<PlantPlacement> plantsPlanted = new ArrayList<>();
    private final List<Integer> killTicks = new ArrayList<>();
    private int zombiesKilled;
    private int sunCollected;
    private int plantsLost;
    private int killsAtColZeroNoMower;
    private int firstWaveTick = -1;
    private int finalSun;

    public void recordKill(int tick) {
        zombiesKilled++;
        killTicks.add(tick);
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

    public int getZombiesKilled() {
        return zombiesKilled;
    }

    public int getSunCollected() {
        return sunCollected;
    }

    public int getPlantsLost() {
        return plantsLost;
    }

    public int getKillsAtColZeroNoMower() {
        return killsAtColZeroNoMower;
    }

    public int getFirstWaveTick() {
        return firstWaveTick;
    }

    public int getFinalSun() {
        return finalSun;
    }

    public void setFinalSun(int finalSun) {
        this.finalSun = finalSun;
    }

    public List<PlantPlacement> getPlantsPlanted() {
        return plantsPlanted;
    }

    public List<Integer> getKillTicks() {
        return killTicks;
    }

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

    public static final class PlantPlacement {
        private final Plants type;
        private final int col;
        private final int row;

        public PlantPlacement(Plants type, int col, int row) {
            this.type = type;
            this.col = col;
            this.row = row;
        }

        public Plants getType() {
            return type;
        }

        public int getCol() {
            return col;
        }

        public int getRow() {
            return row;
        }
    }
}
