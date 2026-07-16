package model.entities.plants.types;

import model.Game;
import model.Vec2;
import model.entities.plants.PlantCombat;
import model.entities.plants.Plants;
import model.entities.plants.Wallnut;

public class ExplodeONut extends Wallnut {

    private boolean exploded = false;

    public ExplodeONut(Vec2 position) {
        super(Plants.EXPLODE_O_NUT, position);
    }

    @Override
    public void onTick(Game game) {
        if (isDead()) explodeOnce(game);
    }

    @Override
    public void onDeath(Game game) {
        explodeOnce(game);
    }

    private void explodeOnce(Game game) {
        if (exploded) return;
        exploded = true;
        PlantCombat.explode(game, getCol(), getRow(), 1, getAttackdamage());
        PlantCombat.removePlant(game, this);
    }

    @Override
    public void onPlantFood(Game game) {
        setHp(getHp() + 1000);
    }
}
