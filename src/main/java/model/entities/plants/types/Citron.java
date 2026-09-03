package model.entities.plants.types;

import model.Game;
import model.Vec2;
import model.entities.plants.Muzzle;
import model.entities.plants.Plants;
import model.entities.plants.Shooter;

import java.util.List;

public class Citron extends Shooter {

    public static final String CHARGING = "charge";

    private static final int VOLLEYS = 4;
    private static final double FOOD_POWER = 25d;
    private static final double CHARGE_TIME = 8d;

    private double charge;

    public Citron(Vec2 position) {
        super(Plants.CITRON, position);
    }

    public boolean isCharged() {
        return charge >= CHARGE_TIME;
    }

    public double chargeFraction() {
        return Math.min(1d, charge / CHARGE_TIME);
    }

    @Override
    public void onTick(Game game) {
        if (!isCharged()) {
            charge += model.Game.SECONDS_PER_TICK;
        }
        super.onTick(game);
    }

    @Override
    protected boolean aims(Game game, Muzzle muzzle) {
        return (isBursting() || isCharged()) && super.aims(game, muzzle);
    }

    @Override
    protected void fireFrom(Game game, Muzzle muzzle) {
        super.fireFrom(game, muzzle);
        charge = 0d;
    }

    @Override
    protected int foodVolleys() {
        return VOLLEYS;
    }

    @Override
    protected double foodDamageFactor() {
        return FOOD_POWER;
    }

    @Override
    public List<Muzzle> ports() {
        return super.ports();
    }
}
