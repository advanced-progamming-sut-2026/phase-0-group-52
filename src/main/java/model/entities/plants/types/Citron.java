package model.entities.plants.types;

import model.Game;
import model.Vec2;
import model.entities.plants.PlantCombat;
import model.entities.plants.Plants;
import model.entities.plants.Shooter;
import model.entities.zombies.Zombie;

public class Citron extends Shooter {

    public Citron(Vec2 position) {
        super(Plants.CITRON, position);
    }

    @Override
    public void onPlantFood(Game game) {
        for (Zombie z : PlantCombat.zombiesInRow(game, getRow()))
            z.takeDamage(9999);
        PlantCombat.removeDeadZombies(game);
    }
}
