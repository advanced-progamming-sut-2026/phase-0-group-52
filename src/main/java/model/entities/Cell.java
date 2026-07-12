package model.entities;

import model.entities.plants.Plant;

import java.util.ArrayList;

/**
 * یک خانه از زمین بازی.
 * می‌تواند حداکثر دو گیاه روی هم داشته باشد (در صورتی که گیاه زیری/رویی این قابلیت را داشته باشد).
 */
public class Cell {
    private CellType type;
    private double hp;                 // فقط برای TOMBSTONE (۷۰۰) و FROZEN (۶۰۰) معنا دارد
    private final ArrayList<Plant> plants = new ArrayList<>(); // حداکثر ۲
    private final int row;
    private final int col;

    public Cell(CellType type, int row, int col) {
        this.row = row;
        this.col = col;
        setType(type);
    }

    /** تغییر نوع خانه و تنظیم HP پیش‌فرض متناسب با آن. */
    public void setType(CellType type) {
        this.type = type;
        switch (type) {
            case TOMBSTONE -> this.hp = 700;
            case FROZEN -> this.hp = 600;
            default -> this.hp = 0;
        }
    }

    public boolean isPlantable() {
        return type.isPlantable() && plants.size() < 2;
    }

    public boolean isEmpty() {
        return plants.isEmpty();
    }

    public CellType getType() { return type; }
    public double getHp() { return hp; }
    public void setHp(double hp) { this.hp = hp; }
    public ArrayList<Plant> getPlants() { return plants; }
    public int getRow() { return row; }
    public int getCol() { return col; }
}
