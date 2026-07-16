package pvz.model.entities.plants;

import pvz.model.Game;
import pvz.model.Vec2;

public class Lobber extends Plant {

    public Lobber(Plants type, Vec2 position) {
        super(type, type.getBaseHP(), type.getCost(), position, type.getDamage());
    }

    @Override
    public void onTick(Game game) {

    }
}
