package model.entities.plants.types;

import model.Vec2;
import model.entities.plants.Muzzle;
import model.entities.plants.Plants;
import model.entities.plants.Shooter;

import java.util.ArrayList;
import java.util.List;

public class MegaGatlingPea extends Shooter {

    private static final int BURST = 4;
    private static final int VOLLEYS = 40;
    private static final double SPACING = 0.2d;

    public MegaGatlingPea(Vec2 position) {
        super(Plants.MEGA_GATLING_PEA, position);
    }

    @Override
    public List<Muzzle> ports() {
        List<Muzzle> all = new ArrayList<Muzzle>();
        all.add(new Muzzle(Muzzle.MAIN, 0, 1));
        for (int shot = 1; shot < BURST; shot++) {
            all.add(new Muzzle("trail" + shot, 0, 1, shot * SPACING));
        }
        return all;
    }

    @Override
    protected int foodVolleys() {
        return VOLLEYS;
    }
}
