package model.entities.plants.types;

import model.Game;
import model.Vec2;
import model.entities.plants.Modifier;
import model.entities.plants.PlantCombat;
import model.entities.plants.Plants;
import model.entities.zombies.Zombie;

public class HypnoShroom extends Modifier {

    private boolean empowered = false;

    public HypnoShroom(Vec2 position) {
        super(Plants.HYPNO_SHROOM, position);
    }

    @Override
    public void onTick(Game game) {
        if (isFrozen()) return;

        for (Zombie z : PlantCombat.zombiesOnCell(game, getCol(), getRow())) {
            z.setHypnotized(true);
            if (empowered) z.setHp(z.getHp() * 5);
            setHp(0);
            PlantCombat.removePlant(game, this);
            return;
        }
    }

    @Override
    public void onPlantFood(Game game) {
        empowered = true;
    }
}
