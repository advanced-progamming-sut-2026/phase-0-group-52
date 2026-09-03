package model.entities.plants.types;

import model.Vec2;
import model.entities.plants.Muzzle;
import model.entities.plants.Plants;
import model.entities.plants.Shooter;

import java.util.Arrays;
import java.util.List;

public class Repeater extends Shooter {

    private static final int VOLLEYS = 26;
    private static final double TRAIL_FRAME = 0.35d;

    public Repeater(Vec2 position) {
        super(Plants.REPEATER, position);
    }

    @Override
    public List<Muzzle> ports() {
        return Arrays.asList(
                new Muzzle(Muzzle.MAIN, 0, 1),
                new Muzzle("trail", 0, 1, TRAIL_FRAME));
    }

    @Override
    protected int foodVolleys() {
        return VOLLEYS;
    }
}
