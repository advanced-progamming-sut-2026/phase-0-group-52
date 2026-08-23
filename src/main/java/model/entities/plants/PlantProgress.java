package model.entities.plants;

public final class PlantProgress {

    private final Plants plant;
    private int packets;
    private int xp;
    private int level = 1;
    private boolean unlocked;

    public PlantProgress(Plants plant) {
        this.plant = plant;
    }

    public Plants getPlant() {
        return plant;
    }

    public int getPackets() {
        return packets;
    }

    public void setPackets(int value) {
        this.packets = Math.max(0, value);
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int value) {
        this.xp = Math.max(0, value);
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int value) {
        this.level = Math.max(1, value);
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public void setUnlocked(boolean value) {
        this.unlocked = value;
    }

    public int xpNeeded() {
        PlantRecord record = PlantData.record(plant);
        if (record == null || isMaxLevel()) {
            return 0;
        }
        return record.getLeveling().xpToLevel(level + 1);
    }

    public boolean isMaxLevel() {
        PlantRecord record = PlantData.record(plant);
        return record != null && level >= record.getLeveling().getMaxLevel();
    }

    public boolean isXpFull() {
        int needed = xpNeeded();
        return needed > 0 && xp >= needed;
    }

    public float xpRatio() {
        int needed = xpNeeded();
        if (needed <= 0) {
            return 1f;
        }
        return Math.min(1f, (float) xp / needed);
    }
}
