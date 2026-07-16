package model.entities.plants.types;

import model.Game;
import model.Vec2;
import model.entities.plants.PlantCombat;
import model.entities.plants.Plants;
import model.entities.plants.Wallnut;

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
