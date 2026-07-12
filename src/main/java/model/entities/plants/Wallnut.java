package model.entities.plants;

import model.Game;
import model.Vec2;

public class Wallnut extends Plant {

    public Wallnut(Plants type, Vec2 position) {
        super(type, type.getBaseHP(), type.getCost(), position, type.getDamage());
    }

    @Override
    public void onTick(Game game) {
        // TODO  رفتار این دسته را پیاده کن.
    }
}
