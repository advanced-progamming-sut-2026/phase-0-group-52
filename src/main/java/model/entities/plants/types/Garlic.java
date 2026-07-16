package model.entities.plants.types;

import model.Game;
import model.Vec2;
import model.entities.plants.PlantCombat;
import model.entities.plants.Plants;
import model.entities.plants.Wallnut;
import model.entities.zombies.Zombie;

public class Garlic extends Wallnut {

    public Garlic(Vec2 position) {
        super(Plants.GARLIC, position);
    }

    @Override
    public void onTick(Game game) {
        if (isFrozen()) return;
        for (Zombie z : PlantCombat.zombiesOnCell(game, getCol(), getRow()))
            divert(game, z);
    }

    @Override
    public void onPlantFood(Game game) {
        for (Zombie z : PlantCombat.zombiesInRow(game, getRow()))
            divert(game, z);
    }

    private void divert(Game game, Zombie z) {
        int rows = (game.getField() != null) ? game.getField().getRows() : 5;
        int target = z.getRow() + (PlantCombat.RANDOM.nextBoolean() ? 1 : -1);
        if (target < 0) target = 1;
        if (target >= rows) target = rows - 2;
        z.setLine(target);
    }
}
