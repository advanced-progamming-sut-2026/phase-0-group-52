package pvz.model.entities.plants.types;

import pvz.model.Game;
import pvz.model.Vec2;
import pvz.model.entities.plants.PlantCombat;
import pvz.model.entities.plants.Plants;
import pvz.model.entities.plants.Shooter;
import pvz.model.entities.zombies.Zombie;

public class FirePeashooter extends Shooter {

    public FirePeashooter(Vec2 position) {
        super(Plants.FIRE_PEASHOOTER, position);
    }

    @Override
    protected void shoot(Game game) {
        PlantCombat.meltFrozenInRow(game, getRow(), getCol());
        Zombie target = PlantCombat.frontmostAhead(game, getRow(), getCol());
        if (target == null) return;
        target.takeDamage(getAttackdamage());
        PlantCombat.removeDeadZombies(game);
    }

    @Override
    public void onPlantFood(Game game) {
        PlantCombat.meltFrozenInRow(game, getRow(), 0);
        for (Zombie z : PlantCombat.zombiesInRow(game, getRow()))
            z.takeDamage(5 * getAttackdamage());
        PlantCombat.removeDeadZombies(game);
    }
}
