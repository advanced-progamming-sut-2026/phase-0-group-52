package model.entities.plants.types;

import model.Game;
import model.Vec2;
import model.entities.plants.PlantCombat;
import model.entities.plants.Plants;
import model.entities.plants.Shooter;
import model.entities.zombies.Zombie;

public class MegaGatlingPea extends Shooter {

    public MegaGatlingPea(Vec2 position) {
        super(Plants.MEGA_GATLING_PEA, position);
    }

    @Override
    protected void shoot(Game game) {
        Zombie target = PlantCombat.frontmostAhead(game, getRow(), getCol());
        if (target == null) return;
        target.takeDamage(4 * shotDamage(game));
        PlantCombat.removeDeadZombies(game);
    }

    @Override
    public void onPlantFood(Game game) {

        for (Zombie z : PlantCombat.zombiesInRow(game, getRow()))
            z.takeDamage(20 * shotDamage(game));
        Zombie target = PlantCombat.frontmostAhead(game, getRow(), getCol());
        if (target != null) target.takeDamage(4 * 20 * shotDamage(game));
        PlantCombat.removeDeadZombies(game);
    }
}
