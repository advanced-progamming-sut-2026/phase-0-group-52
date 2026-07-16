package model.entities.plants.types;

import model.Game;
import model.Vec2;
import model.entities.plants.PlantCombat;
import model.entities.plants.Plants;
import model.entities.plants.Shooter;
import model.entities.zombies.Zombie;

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
