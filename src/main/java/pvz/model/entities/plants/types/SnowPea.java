package pvz.model.entities.plants.types;

import pvz.model.Game;
import pvz.model.Vec2;
import pvz.model.entities.plants.PlantCombat;
import pvz.model.entities.plants.Plants;
import pvz.model.entities.plants.Shooter;
import pvz.model.entities.zombies.Zombie;

public class SnowPea extends Shooter {

    public SnowPea(Vec2 position) {
        super(Plants.SNOW_PEA, position);
    }

    @Override
    protected void shoot(Game game) {
        Zombie target = PlantCombat.frontmostAhead(game, getRow(), getCol());
        if (target == null) return;
        target.takeDamage(shotDamage(game));
        PlantCombat.slow(target);
        PlantCombat.removeDeadZombies(game);
    }

    @Override
    public void onPlantFood(Game game) {
        for (Zombie z : PlantCombat.zombiesInRow(game, getRow())) {
            PlantCombat.freeze(z);
            z.takeDamage(10 * shotDamage(game));
        }
        PlantCombat.removeDeadZombies(game);
    }
}
