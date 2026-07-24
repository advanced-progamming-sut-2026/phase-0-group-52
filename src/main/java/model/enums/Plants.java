package model.enums;

public enum Plants {
    SUNFLOWER("Sunflower", PlantCategory.SUN_PRODUCERS, Tags.DAY, 50, 300, 0, 24, 5);

    private final String name;
    private final PlantCategory category;
    private final Tags tag;
    private final int cost;
    private final int baseHP;
    private final int damage;
    private final int actionInterval;
    private final int recharge;

    Plants(String name, PlantCategory category, Tags tag, int cost, int baseHP, int damage, int actionInterval, int recharge) {
        this.name = name;
        this.category = category;
        this.tag = tag;
        this.cost = cost;
        this.baseHP = baseHP;
        this.damage = damage;
        this.actionInterval = actionInterval;
        this.recharge = recharge;
    }

    public String getName() {
        return name;
    }

    public PlantCategory getCategory() {
        return category;
    }

    public Tags getTag() {
        return tag;
    }

    public int getCost() {
        return cost;
    }

    public int getBaseHP() {
        return baseHP;
    }

    public int getDamage() {
        return damage;
    }

    public int getActionInterval() {
        return actionInterval;
    }

    public int getRecharge() {
        return recharge;
    }
}
