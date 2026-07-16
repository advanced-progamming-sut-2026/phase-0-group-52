package pvz.model.entities.plants.types;

import pvz.model.Game;
import pvz.model.Vec2;
import pvz.model.entities.plants.Modifier;
import pvz.model.entities.plants.PlantCombat;
import pvz.model.entities.plants.Plants;
import pvz.model.entities.zombies.Zombie;

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
