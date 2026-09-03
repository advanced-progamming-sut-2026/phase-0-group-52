package model.entities.plants.types;

import model.Game;
import model.Vec2;
import model.entities.Projectile;
import model.entities.plants.Lobber;
import model.entities.plants.Muzzle;
import model.entities.plants.PlantCombat;
import model.entities.plants.Plants;
import model.entities.zombies.Zombie;
import model.entities.zombies.Zombies;

import java.util.ArrayList;
import java.util.List;

public class CabbagePult extends Lobber {

    public static final double FOOD_DAMAGE = 1800d;

    private static final int VOLLEYS = 1;

    public CabbagePult(Vec2 position) {
        super(Plants.CABBAGE_PULT, position);
    }

    public static boolean tooBigToRain(Zombie zombie) {
        return zombie.getOrigin() == Zombies.ZOMBIE_GARGANTUAR;
    }

    @Override
    protected int foodVolleys() {
        return VOLLEYS;
    }

    @Override
    protected void fireFrom(Game game, Muzzle muzzle) {
        if (!isBursting()) {
            super.fireFrom(game, muzzle);
            return;
        }
        for (Zombie zombie : rainOn(game)) {
            Projectile cabbage = new Projectile(Projectile.Kind.LOB, getType(),
                    zombie.getRow(), getCol() + 0.5, FOOD_DAMAGE, 1, Muzzle.MAIN,
                    Projectile.FED);
            cabbage.from(getRow());
            game.getProjectiles().add(cabbage);
        }
    }

    private List<Zombie> rainOn(Game game) {
        List<Zombie> targets = new ArrayList<Zombie>();
        for (Zombie zombie : game.getZombies()) {
            if (!zombie.isDead() && !tooBigToRain(zombie)) {
                targets.add(zombie);
            }
        }
        return targets;
    }

    @Override
    protected void onFoodBurst(Game game) {
        PlantCombat.removeDeadZombies(game);
    }
}
