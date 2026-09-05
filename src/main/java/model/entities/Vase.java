package model.entities;

import model.entities.plants.Plants;
import model.entities.zombies.Zombies;

public class Vase {

    public enum Content { PLANT, ZOMBIE, MYSTERY }

    private final int column;
    private final int row;
    private final Content content;
    private final Plants plant;
    private final Zombies zombie;

    private boolean broken;

    public Vase(int column, int row, Content content, Plants plant, Zombies zombie) {
        this.column = column;
        this.row = row;
        this.content = content;
        this.plant = plant;
        this.zombie = zombie;
    }

    public int getColumn() {
        return column;
    }

    public int getRow() {
        return row;
    }

    public Content getContent() {
        return content;
    }

    public Plants getPlant() {
        return plant;
    }

    public Zombies getZombie() {
        return zombie;
    }

    public boolean isBroken() {
        return broken;
    }

    public void breakOpen() {
        broken = true;
    }

    public String artName() {
        switch (content) {
            case PLANT:  return "vase_plant";
            case ZOMBIE: return "vase_zombie";
            default:     return "vase";
        }
    }
}
