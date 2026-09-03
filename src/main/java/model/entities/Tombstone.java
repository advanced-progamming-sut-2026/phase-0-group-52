package model.entities;

public class Tombstone {

    public static final double MAX_HP = 400d;
    public static final int STAGES = 4;

    private final int column;
    private final int row;
    private double hp = MAX_HP;

    public Tombstone(int column, int row) {
        this.column = column;
        this.row = row;
    }

    public int getColumn() {
        return column;
    }

    public int getRow() {
        return row;
    }

    public double getHp() {
        return hp;
    }

    public void takeDamage(double amount) {
        hp = Math.max(0d, hp - amount);
    }

    public void shatter() {
        hp = 0d;
    }

    public boolean isDestroyed() {
        return hp <= 0d;
    }

    public int damageStage() {
        if (hp >= MAX_HP) {
            return 0;
        }
        double lost = (MAX_HP - hp) / MAX_HP;
        return Math.min(STAGES, 1 + (int) (lost * STAGES));
    }

    private String bonus;

    public String getBonus() {
        return bonus;
    }

    public void setBonus(String value) {
        this.bonus = value;
    }

    public String clipName() {
        int stage = damageStage();
        return stage == 0 ? "undamaged" : "damage" + Math.min(STAGES, stage);
    }
}
