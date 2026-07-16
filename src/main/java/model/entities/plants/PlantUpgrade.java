package model.entities.plants;

public class PlantUpgrade {

    private final int level;
    private final String type;
    private final double value;
    private final String specialTag;

    public PlantUpgrade(int level, String type, double value, String specialTag) {
        this.level = level;
        this.type = type;
        this.value = value;
        this.specialTag = specialTag;
    }

    public int getLevel() { return level; }
    public String getType() { return type; }
    public double getValue() { return value; }
    public String getSpecialTag() { return specialTag; }

    public String describe() {
        String amount = value == Math.floor(value)
                ? String.valueOf((long) value) : String.valueOf(value);
        switch (type) {
            case "BUFF_HP":              return "+" + amount + " HP";
            case "BUFF_DAMAGE":          return "+" + amount + " damage";
            case "BUFF_COST":            return amount + " sun cost";
            case "BUFF_RECHARGE":        return amount + "s recharge";
            case "BUFF_ACTION_INTERVAL": return amount + "s action interval";
            default:                     return specialTag.isEmpty() ? type : specialTag;
        }
    }
}
