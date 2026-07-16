package pvz.model.entities.plants.types;

import pvz.model.Game;
import pvz.model.Vec2;
import pvz.model.entities.plants.PlantCombat;
import pvz.model.entities.plants.Plants;
import pvz.model.entities.plants.Shooter;
import pvz.model.entities.zombies.Zombie;

public class Repeater extends Shooter {

    public Repeater(Vec2 position) {
        super(Plants.REPEATER, position);
    }

    @Override
    protected void shoot(Game game) {
        Zombie target = PlantCombat.frontmostAhead(game, getRow(), getCol());
        if (target == null) return;
        target.takeDamage(2 * shotDamage(game));
        PlantCombat.removeDeadZombies(game);
    }

    @Override
    public void onPlantFood(Game game) {
        Zombie target = PlantCombat.frontmostAhead(game, getRow(), getCol());
        if (target != null)
            target.takeDamage(60 * shotDamage(game) + 20 * getAttackdamage());
        PlantCombat.removeDeadZombies(game);
    }
}
