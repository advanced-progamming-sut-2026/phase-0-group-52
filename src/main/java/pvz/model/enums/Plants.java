package pvz.model.enums;

public enum Plants {
    SUNFLOWER("Sunflower",PlantCategory.SUN_PRODUCERS,Tags.DAY,50,300,0,24,5);

    private String name;
    private PlantCategory category;
    private Tags tag;
    private int cost;
    private int baseHP;
    private int damage;
    private int actionInterval;
    private int recharge;

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
