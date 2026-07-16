package model.entities.plants;

import model.Game;
import model.Vec2;

public class SunProducer extends Plant {

    public SunProducer(Plants type, Vec2 position) {
        super(type, type.getBaseHP(), type.getCost(), position, type.getDamage());
    }

    @Override
    public void onTick(Game game) {

    }

    @Override
    public void onPlantFood(Game game) {

    }
}
