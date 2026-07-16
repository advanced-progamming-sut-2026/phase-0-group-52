package pvz.model.entities.plants.types;

import pvz.model.Game;
import pvz.model.Vec2;
import pvz.model.entities.plants.PlantCombat;
import pvz.model.entities.plants.Plants;
import pvz.model.entities.plants.Shooter;
import pvz.model.entities.zombies.Zombie;

public class SplitPea extends Shooter {

    public SplitPea(Vec2 position) {
        super(Plants.SPLIT_PEA, position);
    }

    @Override
    protected void shoot(Game game) {
        Zombie ahead = PlantCombat.frontmostAhead(game, getRow(), getCol());
        if (ahead != null) ahead.takeDamage(shotDamage(game));
        Zombie behind = PlantCombat.nearestBehind(game, getRow(), getCol());
        if (behind != null) behind.takeDamage(2 * shotDamage(game));
        PlantCombat.removeDeadZombies(game);
    }

    @Override
    public void onPlantFood(Game game) {
        Zombie ahead = PlantCombat.frontmostAhead(game, getRow(), getCol());
        if (ahead != null) ahead.takeDamage(30 * shotDamage(game));
        Zombie behind = PlantCombat.nearestBehind(game, getRow(), getCol());
        if (behind != null) behind.takeDamage(30 * shotDamage(game));
        PlantCombat.removeDeadZombies(game);
    }
}
