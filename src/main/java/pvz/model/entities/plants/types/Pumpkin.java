package pvz.model.entities.plants.types;

import pvz.model.Game;
import pvz.model.Vec2;
import pvz.model.entities.plants.Plants;
import pvz.model.entities.plants.Wallnut;

public class Pumpkin extends Wallnut {

    public Pumpkin(Vec2 position) {
        super(Plants.PUMPKIN, position);
    }

    @Override
    public void onPlantFood(Game game) {
        setHp(getHp() + 4000);
    }
}
