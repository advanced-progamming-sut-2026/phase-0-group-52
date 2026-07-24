package model.entities;

public class Lawnmower {
    private int line;
    private boolean isactive;

    public Lawnmower(int line, boolean isactive) {
        this.line = line;
        this.isactive = isactive;
    }

    public void destroyZombies(model.Game game) {
        if (!isactive || game == null) return;
        for (model.entities.zombies.Zombie z
                : new java.util.ArrayList<model.entities.zombies.Zombie>(game.getZombies())) {
            if (z.getRow() == line) z.setHp(0);
        }
        model.entities.plants.PlantCombat.removeDeadZombies(game);
        isactive = false;
        System.out.println("The lawnmower in row " + (line + 1) + " cleared the row!");
    }

    public boolean triggerIfZombieAtHouse(model.Game game) {
        if (!isactive || game == null) return false;
        for (model.entities.zombies.Zombie z : game.getZombies()) {
            if (z.getRow() == line && z.getPosition().x <= 0) {
                destroyZombies(game);
                return true;
            }
        }
        return false;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public boolean isIsactive() {
        return isactive;
    }

    public void setIsactive(boolean isactive) {
        this.isactive = isactive;
    }
}
