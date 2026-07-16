package pvz.model.entities.plants.types;

import pvz.model.Game;
import pvz.model.Vec2;
import pvz.model.entities.plants.PlantCombat;
import pvz.model.entities.plants.Plants;
import pvz.model.entities.plants.Wallnut;

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
