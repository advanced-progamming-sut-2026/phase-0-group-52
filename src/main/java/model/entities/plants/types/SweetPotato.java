package model.entities.plants.types;

import model.Game;
import model.Vec2;
import model.entities.plants.PlantCombat;
import model.entities.plants.Plants;
import model.entities.plants.Wallnut;
import model.entities.zombies.Zombie;

public class SweetPotato extends Wallnut {

    private static final double PULL_RANGE = 2;

    public SweetPotato(Vec2 position) {
        super(Plants.SWEET_POTATO, position);
    }

    @Override
    public void onTick(Game game) {
        if (isFrozen()) return;
        for (int r = getRow() - 1; r <= getRow() + 1; r += 2) {
            for (Zombie z : PlantCombat.zombiesInRow(game, r)) {
                double dx = z.getPosition().x - getCol();
                if (dx >= 0 && dx <= PULL_RANGE) z.setLine(getRow());
            }
        }
    }

    @Override
    public void onPlantFood(Game game) {
        for (Zombie z : PlantCombat.zombiesInArea(game, getCol(), getRow(), 2))
            z.setLine(getRow());
        setHp(getType().getBaseHP());
    }
}
