package pvz.model.entities.plants.types;

import pvz.model.Game;
import pvz.model.Vec2;
import pvz.model.entities.plants.PlantCombat;
import pvz.model.entities.plants.Plants;
import pvz.model.entities.plants.Wallnut;

public class SunBean extends Wallnut {

    private static final int SUN_PER_HIT = 5;

    public SunBean(Vec2 position) {
        super(Plants.SUN_BEAN, position);
    }

    @Override
    public void onTick(Game game) {
        if (isFrozen()) return;

        if (!PlantCombat.zombiesOnCell(game, getCol(), getRow()).isEmpty())
            game.addSun(SUN_PER_HIT);
    }

    @Override
    public void onPlantFood(Game game) {
        setHp(getHp() + 1000);
    }
}
