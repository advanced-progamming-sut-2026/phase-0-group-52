package model.entities;

public enum SunType {
    NORMAL(25, 0.80),
    SPECIAL(100, 0.15),
    RADIOACTIVE(150, 0.05);

    private final int amount;
    private final double chance;

    SunType(int amount, double chance) {
        this.amount = amount;
        this.chance = chance;
    }

    public int getAmount() { return amount; }
    public double getChance() { return chance; }

    public static SunType pickRandom(double roll) {
        if (roll < NORMAL.chance) return NORMAL;
        if (roll < NORMAL.chance + SPECIAL.chance) return SPECIAL;
        return RADIOACTIVE;
    }
}
