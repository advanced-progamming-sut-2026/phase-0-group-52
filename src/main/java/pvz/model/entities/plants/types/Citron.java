package pvz.model.entities.plants.types;

import pvz.model.Game;
import pvz.model.Vec2;
import pvz.model.entities.plants.PlantCombat;
import pvz.model.entities.plants.Plants;
import pvz.model.entities.plants.Shooter;
import pvz.model.entities.zombies.Zombie;

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
