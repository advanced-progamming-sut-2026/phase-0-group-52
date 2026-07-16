package pvz.model.entities.plants.types;

import pvz.model.Game;
import pvz.model.Vec2;
import pvz.model.entities.plants.PlantCombat;
import pvz.model.entities.plants.Plants;
import pvz.model.entities.plants.Shooter;
import pvz.model.entities.zombies.Zombie;

public class Threepeater extends Shooter {

    public Threepeater(Vec2 position) {
        super(Plants.THREEPEATER, position);
    }

    @Override
    protected void shoot(Game game) {
        for (int r = getRow() - 1; r <= getRow() + 1; r++) {
            Zombie target = PlantCombat.frontmostAhead(game, r, getCol());
            if (target != null) target.takeDamage(shotDamage(game));
        }
        PlantCombat.removeDeadZombies(game);
    }

    @Override
    public void onPlantFood(Game game) {
        if (game.getField() == null) return;
        for (int r = 0; r < game.getField().getRows(); r++) {
            Zombie target = PlantCombat.frontmostAhead(game, r, 0);
            if (target != null) target.takeDamage(20 * shotDamage(game));
        }
        PlantCombat.removeDeadZombies(game);
    }
}
