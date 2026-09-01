package model.entities;

public class Lawnmower {

    public static final double SPEED = 0.35;
    public static final double START_COLUMN = -0.6;
    public static final double REACH = 1.2;
    public static final double EXIT_MARGIN = 2.75;

    private int line;
    private boolean isactive;
    private boolean running;
    private double column = START_COLUMN;

    public void destroyZombies(model.Game game) {
        if (!isactive || game == null) {
            return;
        }
        running = true;
        for (model.entities.zombies.Zombie z
                : new java.util.ArrayList<model.entities.zombies.Zombie>(game.getZombies())) {
            if (z.getRow() == line && z.getPosition().x <= column + REACH) {
                z.setHp(0);
            }
        }
        model.entities.plants.PlantCombat.removeDeadZombies(game);
        System.out.println("The lawnmower in row " + (line + 1) + " is rolling!");
    }

    public void advance(model.Game game) {
        if (!running || game == null) {
            return;
        }
        column += SPEED;
        for (model.entities.zombies.Zombie z
                : new java.util.ArrayList<model.entities.zombies.Zombie>(game.getZombies())) {
            if (z.getRow() == line && z.getPosition().x <= column + REACH) {
                z.setHp(0);
            }
        }
        model.entities.plants.PlantCombat.removeDeadZombies(game);
        if (game.getField() != null && column > game.getField().getCols() + EXIT_MARGIN) {
            running = false;
            isactive = false;
        }
    }

    public boolean triggerIfZombieAtHouse(model.Game game) {
        if (!isactive || running || game == null) {
            return false;
        }
        for (model.entities.zombies.Zombie z : game.getZombies()) {
            if (z.getRow() == line && z.getPosition().x <= 0) {
                destroyZombies(game);
                return true;
            }
        }
        return false;
    }

    public Lawnmower(int line, boolean isactive) {
        this.line = line;
        this.isactive = isactive;
    }

    public boolean isRunning() {
        return running;
    }

    public double getColumn() {
        return column;
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
