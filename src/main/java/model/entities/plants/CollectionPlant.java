package model.entities.plants;

public class CollectionPlant {
    private final Plants type;
    private final int level;
    private final double maxhp;
    private final double attackdamage;
    private final int cost;
    private final int action_interval;
    private final int recharge;

    public CollectionPlant(Plants type, int level, double maxhp, double attackdamage, int cost, int action_interval, int recharge) {
        this.type = type;
        this.level = level;
        this.maxhp = maxhp;
        this.attackdamage = attackdamage;
        this.cost = cost;
        this.action_interval = action_interval;
        this.recharge = recharge;
    }

    public Plants getType() {
        return type;
    }

    public double getAttackdamage() {
        return attackdamage;
    }

    public int getLevel() {
        return level;
    }

    public double getMaxhp() {
        return maxhp;
    }

    public int getCost() {
        return cost;
    }

    public int getAction_interval() {
        return action_interval;
    }

    public int getRecharge() {
        return recharge;
    }
}
