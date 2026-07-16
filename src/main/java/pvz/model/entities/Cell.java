package pvz.model.entities;

import pvz.model.entities.plants.Plant;

import java.util.ArrayList;

public class Cell {
    private CellType type;
    private double hp;
    private final ArrayList<Plant> plants = new ArrayList<>();
    private final int row;
    private final int col;

    private String graveBonus;

    private boolean necromancy;

    public Cell(CellType type, int row, int col) {
        this.row = row;
        this.col = col;
        setType(type);
    }

    public void setType(CellType type) {
        this.type = type;
        switch (type) {
            case TOMBSTONE:
                this.hp = 700;
                break;
            case FROZEN:
                this.hp = 600;
                break;
            default:
                this.hp = 0;
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

    public String getGraveBonus() { return graveBonus; }
    public void setGraveBonus(String graveBonus) { this.graveBonus = graveBonus; }

    public boolean isNecromancy() { return necromancy; }
    public void setNecromancy(boolean necromancy) { this.necromancy = necromancy; }
}
