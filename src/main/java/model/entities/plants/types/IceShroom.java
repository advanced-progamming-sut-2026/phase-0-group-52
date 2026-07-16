package model.entities.plants.types;

import model.Game;
import model.Vec2;
import model.entities.plants.Explosive;
import model.entities.plants.PlantCombat;
import model.entities.plants.Plants;
import model.entities.zombies.Zombie;

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
