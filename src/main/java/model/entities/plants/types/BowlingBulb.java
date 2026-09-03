package model.entities.plants.types;

import model.Game;
import model.Vec2;
import model.entities.Projectile;
import model.entities.plants.Muzzle;
import model.entities.plants.PlantCombat;
import model.entities.plants.Plants;
import model.entities.plants.Shooter;
import model.entities.zombies.Zombie;

public class BowlingBulb extends Shooter {

    public static final int BULBS = 1;

    private static final double DAMAGE = 40;
    private static final double DELAY = 2;
    private static final double FOOD_POWER = 4d;
    private static final int VOLLEYS = 1;

    public BowlingBulb(Vec2 position) {
        super(Plants.BOWLING_BULB, position);
    }

    public int getBulb() {
        return 1;
    }

    @Override
    protected double rechargeTime() {
        return DELAY;
    }

    @Override
    protected void shoot(Game game) {
        Zombie target = PlantCombat.frontmostAhead(game, getRow(), getCol());
        if (target == null) {
            return;
        }
        super.shoot(game);
    }

    @Override
    protected void fireFrom(Game game, Muzzle muzzle) {
        roll(game, isBursting() ? DAMAGE * FOOD_POWER : DAMAGE);
    }

    @Override
    protected String shotVariant() {
        return isBursting() ? Projectile.FED + 1 : Projectile.BULB + 1;
    }

    @Override
    protected int foodVolleys() {
        return VOLLEYS;
    }

    private void roll(Game game, double damage) {
        Projectile shot = new Projectile(Projectile.Kind.ORB, getType(), getRow(),
                getCol() + 0.5, damage, 1, Muzzle.MAIN, shotVariant());
        shot.from(getRow());
        shot.bouncing(true);
        game.getProjectiles().add(shot);
    }
}
