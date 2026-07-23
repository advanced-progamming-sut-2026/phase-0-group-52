package model.entities.plants.types;

import model.Game;
import model.Vec2;
import model.entities.Cell;
import model.entities.CellType;
import model.entities.plants.Explosive;
import model.entities.plants.PlantCombat;
import model.entities.plants.Plants;
import model.entities.zombies.Zombie;

import java.util.ArrayList;

public class DoomShroom extends Explosive {

    public DoomShroom(Vec2 position) {
        super(Plants.DOOM_SHROOM, position);
    }

    @Override
    public void onPlanted(Game game) {
        for (Zombie z : new ArrayList<Zombie>(game.getZombies()))
            z.takeDamage(getAttackdamage());
        PlantCombat.removeDeadZombies(game);
        setHp(0);
        PlantCombat.removePlant(game, this);
        if (game.getField() != null) {
            Cell cell = game.getField().getCell(getCol(), getRow());
            if (cell != null) cell.setType(CellType.CRATER);
        }
        System.out.println("Doom-shroom devastated the lawn and left an uncultivable crater at ("
                + (getCol() + 1) + ", " + (getRow() + 1) + ").");
    }
}
