package model.entities.plants.types;

import model.Game;
import model.Vec2;
import model.entities.plants.PlantCombat;
import model.entities.plants.Plants;
import model.entities.plants.Shooter;
import model.entities.zombies.Zombie;

public class SnowPea extends Shooter {

    public SnowPea(Vec2 position) {
        super(Plants.SNOW_PEA, position);
    }

    @Override
    protected void onFoodBurst(Game game) {
        for (Zombie zombie : PlantCombat.zombiesInRow(game, getRow())) {
            PlantCombat.freeze(zombie);
        }
    }
}
