package model.entities.plants.types;

import model.Game;
import model.Vec2;
import model.entities.plants.PlantCombat;
import model.entities.plants.Plants;
import model.entities.plants.Shooter;
import model.entities.zombies.Zombie;

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
