package pvz.model.entities.plants;

import java.util.ArrayList;

import java.util.Arrays;

public enum Plants {
    SUNFLOWER("Sunflower", PlantsCategory.SUN_PRODUCER, new ArrayList<>(Arrays.asList(PlantTag.DAY)), 50, 300, 0, 24, 5);

    private final String name;
    private final PlantsCategory category;
    private final ArrayList<PlantTag> tags;
    private final int cost;
    private final int baseHP;
    private final int damage;
    private final int actionInterval;
    private final int recharge;

    Plants(String name, PlantsCategory category, ArrayList<PlantTag> tags,
           int cost, int baseHP, int damage, int actionInterval, int recharge) {
        this.name = name;
        this.category = category;
        this.tags = tags;
        this.cost = cost;
        this.baseHP = baseHP;
        this.damage = damage;
        this.actionInterval = actionInterval;
        this.recharge = recharge;
    }

    public String getName() { return name; }
    public PlantsCategory getCategory() { return category; }
    public ArrayList<PlantTag> getTags() { return tags; }
    public int getCost() { return cost; }
    public int getBaseHP() { return baseHP; }
    public int getDamage() { return damage; }
    public int getActionInterval() { return actionInterval; }
    public int getRecharge() { return recharge; }
}
