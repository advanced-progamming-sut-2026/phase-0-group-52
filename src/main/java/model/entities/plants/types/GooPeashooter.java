package model.entities.plants.types;

import model.Game;
import model.Vec2;
import model.entities.plants.PlantCombat;
import model.entities.plants.Plants;
import model.entities.plants.Shooter;
import model.entities.zombies.Zombie;

public class GooPeashooter extends Shooter {

    public GooPeashooter(Vec2 position) {
        super(Plants.GOO_PEASHOOTER, position);
    }

    @Override
    protected void shoot(Game game) {
        Zombie target = PlantCombat.frontmostAhead(game, getRow(), getCol());
        if (target == null) return;
        target.takeDirectDamage(shotDamage(game));
        PlantCombat.removeDeadZombies(game);
    }

    @Override
    public void onPlantFood(Game game) {
        for (Zombie z : PlantCombat.zombiesInRow(game, getRow()))
            z.takeDirectDamage(5 * shotDamage(game));
        PlantCombat.removeDeadZombies(game);
    }
}
