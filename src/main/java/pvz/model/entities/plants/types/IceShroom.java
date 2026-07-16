package pvz.model.entities.plants.types;

import pvz.model.Game;
import pvz.model.Vec2;
import pvz.model.entities.plants.Explosive;
import pvz.model.entities.plants.PlantCombat;
import pvz.model.entities.plants.Plants;
import pvz.model.entities.zombies.Zombie;

public class IceShroom extends Explosive {

    public IceShroom(Vec2 position) {
        super(Plants.ICE_SHROOM, position);
    }

    @Override
    public void onPlanted(Game game) {
        for (Zombie z : game.getZombies())
            PlantCombat.freeze(z);
        setHp(0);
        PlantCombat.removePlant(game, this);
    }
}
