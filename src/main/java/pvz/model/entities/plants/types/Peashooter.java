package pvz.model.entities.plants.types;

import pvz.model.Game;
import pvz.model.Vec2;
import pvz.model.entities.plants.PlantCombat;
import pvz.model.entities.plants.Plants;
import pvz.model.entities.plants.Shooter;
import pvz.model.entities.zombies.Zombie;

public class Peashooter extends Shooter {

    public Peashooter(Vec2 position) {
        super(Plants.PEASHOOTER, position);
    }

    @Override
    public void onPlantFood(Game game) {
        Zombie target = PlantCombat.frontmostAhead(game, getRow(), getCol());
        if (target != null) target.takeDamage(30 * shotDamage(game));
        PlantCombat.removeDeadZombies(game);
    }
}
