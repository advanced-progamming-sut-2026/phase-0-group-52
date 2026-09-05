package model.entities;

public enum CellType {
    NORMAL,
    TOMBSTONE,
    WATER,
    SLIPPERY_UP,
    SLIPPERY_DOWN,
    FROZEN,
    LOW_GROUND,
    NECROMANCY,
    CRATER,
    BURNING;

    public boolean isPlantable() {
        return this == NORMAL || this == LOW_GROUND || this == NECROMANCY;
    }

    public boolean isWater() {
        return this == WATER;
    }

    public boolean isBurning() {
        return this == BURNING;
    }
}
