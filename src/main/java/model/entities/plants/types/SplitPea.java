package model.entities.plants.types;

import model.Game;
import model.Vec2;
import model.entities.plants.Muzzle;
import model.entities.plants.PlantCombat;
import model.entities.plants.Plants;
import model.entities.plants.Shooter;

import java.util.Arrays;
import java.util.List;

public class SplitPea extends Shooter {

    public static final String FRONT = "front";
    public static final String BACK = "back";

    private static final double BACK_FRAME = 0.3d;

    public SplitPea(Vec2 position) {
        super(Plants.SPLIT_PEA, position);
    }

    @Override
    public List<Muzzle> ports() {
        return Arrays.asList(
                new Muzzle(FRONT, 0, 1),
                new Muzzle(BACK, 0, -1, BACK_FRAME));
    }

    @Override
    protected boolean aims(Game game, Muzzle muzzle) {
        return isBursting() || covers(game, muzzle);
    }

    private boolean covers(Game game, Muzzle muzzle) {
        return BACK.equals(muzzle.getName())
                ? PlantCombat.nearestBehind(game, getRow(), getCol()) != null
                : PlantCombat.frontmostAhead(game, getRow(), getCol()) != null;
    }
}
