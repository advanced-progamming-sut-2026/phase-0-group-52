package pvz.model.entities.plants.types;

import pvz.model.Game;
import pvz.model.Vec2;
import pvz.model.entities.plants.Plants;
import pvz.model.entities.plants.Wallnut;

public class TallNut extends Wallnut {

    public TallNut(Vec2 position) {
        super(Plants.TALL_NUT, position);
    }

    @Override
    public void onPlantFood(Game game) {
        setHp(getHp() + 8000);
    }
}
