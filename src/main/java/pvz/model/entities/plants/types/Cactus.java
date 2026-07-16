package pvz.model.entities.plants.types;

import pvz.model.Game;
import pvz.model.Vec2;
import pvz.model.entities.plants.Plants;
import pvz.model.entities.plants.StrikeThrough;

public class Cactus extends StrikeThrough {

    public Cactus(Vec2 position) {
        super(Plants.CACTUS, position);
    }

    @Override
    protected int maxTargets() { return 3; }

    @Override
    public void onPlantFood(Game game) {

        pierce(game, Integer.MAX_VALUE, Double.MAX_VALUE, 2 * getAttackdamage());
    }
}
