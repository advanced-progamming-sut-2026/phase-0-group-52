package pvz.model.entities.plants;

import pvz.model.Game;
import pvz.model.Vec2;

public class Homing extends Plant {

    public Homing(Plants type, Vec2 position) {
        super(type, type.getBaseHP(), type.getCost(), position, type.getDamage());
    }

    @Override
    public void onTick(Game game) {

    }
}
