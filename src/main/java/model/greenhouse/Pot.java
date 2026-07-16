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

    public void plantMarigold() {
        occupied = true;
        marigold = true;
        plantType = null;
        readyAtMillis = System.currentTimeMillis() + 2 * Greenhouse.HOUR_MILLIS;
    }

    public void plantSpecial(Plants plant) {
        occupied = true;
        marigold = false;
        plantType = plant;
        readyAtMillis = System.currentTimeMillis() + 8 * Greenhouse.HOUR_MILLIS;
    }

    public boolean isReady() {
        return occupied && System.currentTimeMillis() >= readyAtMillis;
    }

    public int remainingHoursCeil() {
        long remaining = readyAtMillis - System.currentTimeMillis();
        if (remaining <= 0) return 0;
        return (int) ((remaining + Greenhouse.HOUR_MILLIS - 1) / Greenhouse.HOUR_MILLIS);
    }

    public void finishGrowth() {
        readyAtMillis = System.currentTimeMillis();
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
