package pvz.model.entities.plants;

import java.util.ArrayList;

import java.util.Arrays;

public enum Plants{
    SUNFLOWER("Sunflower", PlantsCategory.SUN_PRODUCER, new ArrayList<>(Arrays.asList(PlantTag.DAY)), 50, 300, 0, 24, 5);

    private final String name;
    private final PlantsCategory category;
    private final ArrayList<PlantTag> tags;
    private final int basecost;
    private final int baseHP;
    private final int basedamage;
    private final int base_action_interval;
    private final int base_recharge;
    private final String description;

    Plants(String name, PlantsCategory category, ArrayList<PlantTag> tags,
           int cost, int baseHP, int damage, int actionInterval, int recharge, String description) {
        this.name = name;
        this.category = category;
        this.tags = tags;
        this.basecost = cost;
        this.baseHP = baseHP;
        this.basedamage = damage;
        this.base_action_interval = actionInterval;
        this.base_recharge = recharge;
        this.description = description;
    }

    public String getName() { return name; }
    public PlantsCategory getCategory() { return category; }
    public ArrayList<PlantTag> getTags() { return tags; }
    public int getBasecost() { return basecost; }
    public int getBaseHP() { return baseHP; }
    public int getBasedamage() { return basedamage; }
    public int getBase_action_interval() { return base_action_interval; }
    public int getBase_recharge() { return base_recharge; }
    public String getDescription(){
        return description;
    }
}
