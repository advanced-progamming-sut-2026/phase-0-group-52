package pvz.model.entities.plants;

public enum Plants {
    SUNFLOWER("Sunflower","Sun Producer","Day",50,300,0,24,5);

    private String name;
    private String category;
    private String tag;
    private int cost;
    private int baseHP;
    private int damage;
    private int actionInterval;
    private int recharge;

    Plants(String name, String category, String tag, int cost, int baseHP, int damage, int actionInterval, int recharge) {
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

    public String getCategory() {
        return category;
    }

    public String getTag() {
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
