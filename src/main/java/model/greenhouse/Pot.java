package model.greenhouse;

import model.entities.plants.Plants;

public class Pot {

    private final int x;
    private final int y;
    private boolean unlocked;
    private boolean occupied;
    private boolean marigold;
    private Plants plantType;
    private long readyAtMillis;
    private long plantedAtMillis;

    public Pot(int x, int y, boolean unlocked) {
        this.x = x;
        this.y = y;
        this.unlocked = unlocked;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public boolean isUnlocked() { return unlocked; }
    public void setUnlocked(boolean unlocked) { this.unlocked = unlocked; }
    public boolean isOccupied() { return occupied; }
    public boolean isMarigold() { return marigold; }
    public Plants getPlantType() { return plantType; }

    public static final long GROW_MILLIS = 30L * 60L * 1000L;
    public static final float SEEDLING_SCALE = 0.35f;

    public void plantMarigold() {
        sow(true, null);
    }

    public void plantSpecial(Plants plant) {
        sow(false, plant);
    }

    private void sow(boolean isMarigold, Plants plant) {
        occupied = true;
        marigold = isMarigold;
        plantType = plant;
        plantedAtMillis = System.currentTimeMillis();
        readyAtMillis = plantedAtMillis + GROW_MILLIS;
    }

    public long getPlantedAtMillis() {
        return plantedAtMillis;
    }

    public void setTimestamps(long plantedAt, long readyAt) {
        this.plantedAtMillis = plantedAt;
        this.readyAtMillis = readyAt;
    }

    public long getReadyAtMillis() {
        return readyAtMillis;
    }

    public double growth() {
        if (!occupied) {
            return 0d;
        }
        long span = readyAtMillis - plantedAtMillis;
        if (span <= 0L) {
            return 1d;
        }
        double done = (System.currentTimeMillis() - plantedAtMillis) / (double) span;
        return Math.max(0d, Math.min(1d, done));
    }

    public long remainingMillis() {
        return Math.max(0L, readyAtMillis - System.currentTimeMillis());
    }

    public void hasten(long millis) {
        readyAtMillis = Math.max(System.currentTimeMillis(), readyAtMillis - millis);
    }

    public String caption() {
        if (!unlocked) {
            return "";
        }
        if (!occupied) {
            return "empty";
        }
        String name = marigold ? "Marigold" : plantType.getName();
        if (isReady()) {
            return name + " - fully grown";
        }
        long left = remainingMillis();
        long minutes = (left + 59999L) / 60000L;
        return name + " - " + (minutes >= 1 ? minutes + "m" : (left / 1000L) + "s");
    }

    public float visualScale() {
        return SEEDLING_SCALE + (1f - SEEDLING_SCALE) * (float) growth();
    }

    public boolean holdsWaterPlant() {
        return plantType != null
                && plantType.getTags().contains(model.entities.plants.PlantTag.WATER);
    }

    public boolean isReady() {
        return occupied && System.currentTimeMillis() >= readyAtMillis;
    }

    public int remainingHoursCeil() {
        long remaining = readyAtMillis - System.currentTimeMillis();
        if (remaining <= 0) return 0;
        return (int) ((remaining + Greenhouse.HOUR_MILLIS - 1) / Greenhouse.HOUR_MILLIS);
    }

    public void restart() {
        if (!occupied) {
            return;
        }
        plantedAtMillis = System.currentTimeMillis();
        readyAtMillis = plantedAtMillis + GROW_MILLIS;
    }

    public void finishGrowth() {
        readyAtMillis = System.currentTimeMillis();
        plantedAtMillis = Math.min(plantedAtMillis, readyAtMillis - 1L);
    }

    public void clear() {
        occupied = false;
        marigold = false;
        plantType = null;
    }

    public String describe() {
        if (!unlocked) return "locked";
        if (!occupied) return "empty";
        String name = marigold ? "Marigold" : plantType.getName();
        if (isReady()) return name + " [ready]";
        long minutes = (readyAtMillis - System.currentTimeMillis() + 59999) / 60000;
        return name + " [" + (minutes / 60) + "h " + (minutes % 60) + "m left]";
    }
}
