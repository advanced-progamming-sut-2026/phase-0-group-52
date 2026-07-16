package pvz.model.entities.plants;

import pvz.model.Game;
import pvz.model.Vec2;

public class Melee extends Plant {

    public Melee(Plants type, Vec2 position) {
        super(type, type.getBaseHP(), type.getCost(), position, type.getDamage());
    }

    @Override
    public void onTick(Game game) {

    }
}
