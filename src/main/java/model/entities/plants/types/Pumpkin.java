package model.entities.plants.types;

import model.Game;
import model.Vec2;
import model.entities.plants.Plants;
import model.entities.plants.Wallnut;

public class Pumpkin extends Wallnut {

    public Pumpkin(Vec2 position) {
        super(Plants.PUMPKIN, position);
    }

    @Override
    public void onPlantFood(Game game) {
        setHp(getHp() + 4000);
    }
}
