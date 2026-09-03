package model.entities.plants.types;

import model.Game;
import model.Vec2;
import model.entities.plants.Muzzle;
import model.entities.plants.PlantCombat;
import model.entities.plants.Plants;
import model.entities.plants.Shooter;
import model.entities.zombies.Zombie;

public class FirePeashooter extends Shooter {

    public static final String FLAME = "flame";

    private static final double LANE_BURN = 0.6d;

    public FirePeashooter(Vec2 position) {
        super(Plants.FIRE_PEASHOOTER, position);
    }

    @Override
    protected void shoot(Game game) {
        PlantCombat.meltFrozenInRow(game, getRow(), getCol());
        super.shoot(game);
    }

    @Override
    public java.util.List<Muzzle> portsFor(String state) {
        return model.entities.Projectile.FED.equals(state)
                ? java.util.Collections.singletonList(new Muzzle(FLAME, 0, 1))
                : ports();
    }

    @Override
    protected void fireFrom(Game game, Muzzle muzzle) {
        if (!FLAME.equals(muzzle.getName())) {
            super.fireFrom(game, muzzle);
            return;
        }
        for (Zombie zombie : PlantCombat.zombiesInRow(game, getRow())) {
            if (zombie.getPosition().x >= getCol()) {
                zombie.takeDamage(getAttackdamage() * LANE_BURN);
            }
        }
        PlantCombat.removeDeadZombies(game);
    }

    @Override
    protected void onFoodBurst(Game game) {
        PlantCombat.meltFrozenInRow(game, getRow(), 0);
    }
}
