package model.entities.plants.types;

import model.Game;
import model.Vec2;
import model.entities.plants.Plants;
import model.entities.plants.Wallnut;

public class TallNut extends Wallnut {

    public TallNut(Vec2 position) {
        super(Plants.TALL_NUT, position);
    }

    @Override
    public void onPlantFood(Game game) {
        setHp(getHp() + 8000);
    }
}
