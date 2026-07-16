package model.entities.plants.types;

import model.Game;
import model.Vec2;
import model.entities.plants.PlantCombat;
import model.entities.plants.Plants;
import model.entities.plants.Shooter;
import model.entities.zombies.Zombie;

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
